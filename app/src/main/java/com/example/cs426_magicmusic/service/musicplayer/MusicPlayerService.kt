package com.example.cs426_magicmusic.service.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants
import com.example.cs426_magicmusic.ui.view.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Service to handle music playback, the same as PlayAudioManager, but set up as a service
 * to allow for background playback and notifications
 */

class MusicPlayerService : LifecycleService() {
    companion object {
        const val CHANNEL_ID = "music_channel"
        const val NOTIFICATION_ID = 1
    }

    private var currentJob = lifecycleScope.launch {}
    private var playlist: MutableList<Song> = mutableListOf()
    private var currentSongIndex = 0
    private var playerMode = Constants.PlayerMode.REPEAT_ALL // default mode
    private val binder = LocalBinder()
    private var mediaPlayer: MediaPlayer = MediaPlayer()

    private val _currentSongLiveData = MutableLiveData<Song>()
    val currentSongLiveData: LiveData<Song> = _currentSongLiveData

    private val _isPlayingLiveData = MutableLiveData<Boolean>()
    val isPlayingLiveData: LiveData<Boolean> = _isPlayingLiveData

    private val _currentSongPositionLiveData = MutableLiveData<Int>()
    val currentSongPositionLiveData: LiveData<Int> = _currentSongPositionLiveData

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        Log.d("MusicPlayerService", "onCreate called")
        super.onCreate()

        startForegroundServiceWithNotification()
        // Optional: Handle when the MediaPlayer is ready
        mediaPlayer.setOnPreparedListener {
            it.start()
        }
    }

    private fun playSong(song: Song) {
        Log.d("playSong", song.title)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(song.path)
        mediaPlayer.prepareAsync()

        // Update the LiveData for the current song
        // so that ViewModel can observe it
        setCurrentPosition(0)
        mediaPlayer.setOnCompletionListener {
            _isPlayingLiveData.value = false
        }
        _currentSongLiveData.postValue(song)
        _isPlayingLiveData.postValue(true)
    }

    // Function to play a song based on the mode
    fun playNextSong() {
        when (playerMode) {
            Constants.PlayerMode.SHUFFLE -> {
                currentSongIndex =  Random.nextInt(playlist.size)
            }
            Constants.PlayerMode.REPEAT -> {
                // Do nothing, same song will repeat
            }
            Constants.PlayerMode.REPEAT_ALL -> {
                currentSongIndex = (currentSongIndex + 1) % playlist.size
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
        stopForegroundService()
        mediaPlayer.release()
    }

    private fun startForegroundServiceWithNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java) // Intent to open the app
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Playing")  // Notification title
            .setContentText("Your song is playing")  // Notification content
            .setSmallIcon(R.drawable.placeholder_default)  // Notification icon
            .setContentIntent(pendingIntent)  // PendingIntent to handle notification click
            .setPriority(NotificationCompat.PRIORITY_LOW)  // Set low priority to avoid showing heads-up
            .build()

        createNotificationChannel()  // Setup the notification channel
        startForeground(NOTIFICATION_ID, notification)
        Log.d("MusicPlayerService", "startForegroundServiceWithNotification called")
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_DETACH) // Stops the foreground service and removes the notification
        stopSelf() // Optionally stop the service if you want
    }

    // Create a notification channel for Android 8.0 and above
    private fun createNotificationChannel() {
        Log.d("MusicPlayerService", "createNotificationChannel called")
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Music Service Channel",
            NotificationManager.IMPORTANCE_LOW  // Use low importance to avoid visual interruptions
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }
}