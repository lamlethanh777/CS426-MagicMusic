package com.example.cs426_magicmusic.service.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_PAUSE
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_NEXT
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_PREVIOUS
import com.example.cs426_magicmusic.others.Constants.CURRENT_SONG
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_DESCRIPTION
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_ID
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_NAME
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_CONTENT_TEXT
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_CONTENT_TITLE
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_ID
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_REQUEST_CODE
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_ARTIST
import com.example.cs426_magicmusic.ui.view.songplayer.SongPlayerActivity
import com.example.cs426_magicmusic.utils.ImageUtility
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Service to handle music playback, the same as PlayAudioManager, but set up as a service
 * to allow for background playback and notifications
 */

class MusicPlayerService : LifecycleService() {
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val _currentSongLiveData = MutableLiveData<Song>()
    val currentSongLiveData: LiveData<Song> = _currentSongLiveData

    private val _isPlayingLiveData = MutableLiveData<Boolean>()
    val isPlayingLiveData: LiveData<Boolean> = _isPlayingLiveData

    private val _currentSongPositionLiveData = MutableLiveData<Int>()
    val currentSongPositionLiveData: LiveData<Int> = _currentSongPositionLiveData

    private var currentJob = lifecycleScope.launch {}
    private var playlist: MutableList<Song> = mutableListOf()
    private var currentSongIndex = 0
    private var playerMode = Constants.PlayerMode.REPEAT_ALL

    lateinit var remoteViews: RemoteViews
    lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var musicBroadcastReceiver: MusicBroadcastReceiver
    private var isReceiverRegistered = false
    private var isPlayingSong = false

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayerService", "onCreate called")

        mediaPlayer.setOnPreparedListener {
            it.start()
        }

        // Register the BroadcastReceiver
        musicBroadcastReceiver = MusicBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_SKIP_NEXT)
            addAction(ACTION_SKIP_PREVIOUS)
        }
        registerReceiver(musicBroadcastReceiver, filter)
        isReceiverRegistered = true
    }

    private fun playSong(song: Song) {
        Log.d("playSong", song.title)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.path)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnCompletionListener {
            _isPlayingLiveData.value = false
        }

        // Update the LiveData for the current song so that ViewModel can observe it
        setCurrentPosition(0)
        _currentSongLiveData.postValue(song)
        _isPlayingLiveData.postValue(true)

        // If the song is not playing, we don't create the notification
        if (!isPlayingSong) {
            Log.d("playSong", "Creating notification")
            createNotification()
            isPlayingSong = true
        }

        // Update the notification with the new song information
        updateNotification(song)
    }

    // Function to play a song based on the mode
    fun playNextSong(offset: Int = CURRENT_SONG) {
        when (playerMode) {
            Constants.PlayerMode.SHUFFLE -> {
                currentSongIndex = Random.nextInt(playlist.size)
            }

            Constants.PlayerMode.REPEAT -> {
                // Do nothing, same song will repeat
            }

            Constants.PlayerMode.REPEAT_ALL -> {
                currentSongIndex = (currentSongIndex + offset) % playlist.size
            }

            Constants.PlayerMode.NONE -> {
                // Do nothing
            }
        }
        playSong(playlist[currentSongIndex])
    }

    fun pauseSong() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _isPlayingLiveData.postValue(false)
        }
    }

    fun resume() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            _isPlayingLiveData.postValue(true)
        }
    }

    private fun getSongDuration(): Int {
        return mediaPlayer.duration
    }

    fun setCurrentPosition(position: Int) {
        var safePosition = if (position < 0) 0 else position
        if (safePosition > getSongDuration()) {
            safePosition = getSongDuration()
        }
        mediaPlayer.seekTo(safePosition)

        // Update the LiveData for the current position
        currentJob.cancel()
        currentJob = lifecycleScope.launch {
            while (isActive) {
                _currentSongPositionLiveData.postValue(mediaPlayer.currentPosition)
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

    fun getCurrentSong(): Song {
        return playlist[currentSongIndex]
    }

    fun setCurrentSong(songIndex: Int) {
        if (songIndex < 0 || songIndex > playlist.size) {
            return
        }
        currentSongIndex = songIndex
    }

    fun setPlayerMode(mode: Constants.PlayerMode) {
        playerMode = mode
    }

    fun setPlaylist(songs: List<Song>) {
        playlist.clear()
        playlist.addAll(songs)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(musicBroadcastReceiver)
            isReceiverRegistered = false
        }
        stopForegroundService()
        mediaPlayer.release()
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_DETACH) // Stops the foreground service and removes the notification
        stopSelf() // Optionally stop the service if you want
    }

    private fun createNotification() {
        val channel = NotificationChannel(
            PLAYER_CHANNEL_ID,
            PLAYER_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = PLAYER_CHANNEL_DESCRIPTION
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val flag = PendingIntent.FLAG_IMMUTABLE

        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            PLAYER_NOTIFICATION_REQUEST_CODE,
            Intent(ACTION_PLAY_PAUSE).setPackage(packageName),
            flag
        )
        val nextIntent = PendingIntent.getBroadcast(
            this,
            PLAYER_NOTIFICATION_REQUEST_CODE,
            Intent(ACTION_SKIP_NEXT).setPackage(packageName),
            flag
        )
        val previousIntent = PendingIntent.getBroadcast(
            this,
            PLAYER_NOTIFICATION_REQUEST_CODE,
            Intent(ACTION_SKIP_PREVIOUS).setPackage(packageName),
            flag
        )

        // Create a PendingIntent to return to the app's main activity
        val contentIntent = PendingIntent.getActivity(
            this, 0, packageManager.getLaunchIntentForPackage(packageName), flag
        )

        remoteViews = RemoteViews(packageName, R.layout.notification_music_player).apply {
            setOnClickPendingIntent(R.id.notification_play_pause, playPauseIntent)
            setOnClickPendingIntent(R.id.notification_skip_next, nextIntent)
            setOnClickPendingIntent(R.id.notification_skip_previous, previousIntent)
        }

        notificationBuilder = NotificationCompat.Builder(this, PLAYER_CHANNEL_ID)
            .setSmallIcon(R.drawable.placeholder_default)
            .setContentTitle(PLAYER_NOTIFICATION_CONTENT_TITLE)
            .setContentText(PLAYER_NOTIFICATION_CONTENT_TEXT)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setOngoing(true)

        // Start the service in the foreground
        startForeground(PLAYER_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updateNotification(song: Song) {
        val notificationManager = NotificationManagerCompat.from(this)

        // Update the RemoteViews with the new song information
        remoteViews.setTextViewText(R.id.notification_song_title, song.title)
        remoteViews.setTextViewText(R.id.notification_song_artists, song.artistNames)

        val songImage = ImageUtility.loadBitmap(this, song.uri)
        Log.d("updateNotification", "thumbnail: $songImage")
        if (songImage != null) {
            remoteViews.setImageViewBitmap(R.id.notification_song_image, songImage)
        } else {
            remoteViews.setImageViewResource(
                R.id.notification_song_image,
                R.drawable.ic_music_note
            )
        }

        // Notify the notification manager to update the notification
        notificationManager.notify(1, notificationBuilder.build())
    }
}