package com.example.cs426_magicmusic.service.music_player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_PAUSE
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_NEXT
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_PREVIOUS
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_DESCRIPTION
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_ID
import com.example.cs426_magicmusic.others.Constants.PLAYER_CHANNEL_NAME
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_CONTENT_TEXT
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_CONTENT_TITLE
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_ID
import com.example.cs426_magicmusic.others.Constants.PLAYER_NOTIFICATION_REQUEST_CODE
import com.example.cs426_magicmusic.utils.ImageUtility

class MusicPlayerService : LifecycleService() {
    private val binder = LocalBinder()
    private lateinit var musicPlayer: MusicPlayer

    lateinit var remoteViews: RemoteViews
    lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var musicBroadcastReceiver: MusicBroadcastReceiver
    private var isReceiverRegistered = false
    private var isPlayingSong = false

    // Expose LiveData from MusicPlayer
    val currentSongLiveData: LiveData<Song> get() = musicPlayer.currentSongLiveData
    val isPlayingLiveData: LiveData<Boolean> get() = musicPlayer.isPlayingLiveData
    val currentSongPositionLiveData: LiveData<Int> get() = musicPlayer.currentSongPositionLiveData
    val repeatModeLiveData: LiveData<Int> get() = musicPlayer.repeatModeLiveData
    val shuffleModeLiveData: LiveData<Boolean> get() = musicPlayer.shuffleModeLiveData
    val alarmModeLiveData: LiveData<Boolean> get() = musicPlayer.shuffleModeLiveData

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        musicPlayer = MusicPlayer(this)
        Log.d("MusicPlayerService", "onCreate called")

        // Register the BroadcastReceiver
        musicBroadcastReceiver = MusicBroadcastReceiver()
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_SKIP_NEXT)
            addAction(ACTION_SKIP_PREVIOUS)
        }
        ContextCompat.registerReceiver(
            this,
            musicBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isReceiverRegistered = true

        Log.d("MusicPlayerService", "onCreate done")
    }

    fun playCurrent() {
        musicPlayer.playCurrent()
        createOneTimeNotification()
    }

    fun playNext() {
        musicPlayer.playNext()
        createOneTimeNotification()
    }

    fun playPrevious() {
        musicPlayer.playPrevious()
        createOneTimeNotification()
    }

    fun pause() {
        musicPlayer.pause()
    }

    fun resume() {
        musicPlayer.resume()
    }

    fun seekTo(position: Int) {
        musicPlayer.seekTo(position)
    }

    fun setPlaylist(songs: List<Song>, songIndex: Int) {
        musicPlayer.setPlaylist(songs, songIndex)
    }

    fun setNextShuffleMode() {
        musicPlayer.setNextShuffleMode()
    }

    fun setNextRepeatMode() {
        musicPlayer.setNextRepeatMode()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverRegistered) {
            unregisterReceiver(musicBroadcastReceiver)
            isReceiverRegistered = false
        }
        stopForegroundService()
        musicPlayer.release()

        Log.d("MusicPlayerService", "onDestroy done")
    }

    private fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun createOneTimeNotification() {
        if (!isPlayingSong) {
            createNotification()
            isPlayingSong = true
        }
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

        val contentIntent = PendingIntent.getActivity(
            this,
            PLAYER_NOTIFICATION_REQUEST_CODE,
            packageManager.getLaunchIntentForPackage(packageName),
            flag
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

        startForeground(PLAYER_NOTIFICATION_ID, notificationBuilder.build())

        musicPlayer.currentSongLiveData.observe(this) {
            updateSongInformationOfNotification(it)
        }

        musicPlayer.isPlayingLiveData.observe(this) {
            updatePlayPauseButtonOfNotification(it)
        }
    }

    private fun updateSongInformationOfNotification(song: Song) {
        val notificationManager = NotificationManagerCompat.from(this)

        remoteViews.setTextViewText(R.id.notification_song_title, song.title)
        remoteViews.setTextViewText(R.id.notification_song_artists, song.artistNames)

        val songImage = ImageUtility.loadBitmapFromUri(this, song.uri)
        Log.d("updateNotification", "thumbnail: $songImage")
        if (songImage != null) {
            remoteViews.setImageViewBitmap(R.id.notification_song_image, songImage)
        } else {
            remoteViews.setImageViewResource(
                R.id.notification_song_image,
                R.drawable.ic_music_note
            )
        }

        notificationManager.notify(PLAYER_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun updatePlayPauseButtonOfNotification(isPlay: Boolean) {
        val notificationManager = NotificationManagerCompat.from(this)

        if (isPlay) {
            remoteViews.setImageViewResource(
                R.id.notification_play_pause, R.drawable.ic_pause_circle_40
            )
        } else {
            remoteViews.setImageViewResource(
                R.id.notification_play_pause, R.drawable.ic_play_circle_40
            )
        }

        notificationManager.notify(PLAYER_NOTIFICATION_ID, notificationBuilder.build())
    }
}