package com.example.cs426_magicmusic.service.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_PAUSE
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_NEXT
import com.example.cs426_magicmusic.others.Constants.ACTION_SKIP_PREVIOUS
import com.example.cs426_magicmusic.others.Constants.PLAY_DIRECTION_NEXT
import com.example.cs426_magicmusic.others.Constants.PLAY_DIRECTION_PREVIOUS

class MusicBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = (context as MusicPlayerService)
        val notificationManager = NotificationManagerCompat.from(context)
        val remoteViews = service.remoteViews

        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                if (service.isPlayingLiveData.value == true) {
                    service.pause()
                    remoteViews.setImageViewResource(
                        R.id.notification_play_pause,
                        R.drawable.ic_play_circle_40
                    )
//                    service.notificationBuilder.setOngoing(false)
                } else {
                    service.resume()
                    remoteViews.setImageViewResource(
                        R.id.notification_play_pause,
                        R.drawable.ic_pause_circle_40
                    )
//                    service.notificationBuilder.setOngoing(true)
                }
                notificationManager.notify(1, service.notificationBuilder.build())
            }

            ACTION_SKIP_NEXT -> service.playNext()

            ACTION_SKIP_PREVIOUS -> service.playPrevious()
        }
    }
}