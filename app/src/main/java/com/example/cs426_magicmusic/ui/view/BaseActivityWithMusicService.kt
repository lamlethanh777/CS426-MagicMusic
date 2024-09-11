package com.example.cs426_magicmusic.ui.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cs426_magicmusic.R

/**
 * Base activity for activities that need to interact with the music service
 * As SongPlayerActivity and MainActivity are almost the same
 * NOT IMPLEMENTED YET
 */

open class BaseActivityWithMusicService : AppCompatActivity() {
//    protected var isServiceBound = false
//    protected lateinit var musicService: MusicPlayerService
//    protected lateinit var baseViewModelWithMusicService: BaseViewModelWithMusicService
//
//    private val serviceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as MusicPlayerService.LocalBinder
//            musicService = binder.getService()
//            baseViewModelWithMusicService.setMusicService(musicService)
//            isServiceBound = true
//
//            when (incomingIntentAction) {
//                Constants.ACTION_PLAY_NEW_SONG -> {
//                    musicService.setPlaylist(songList)
//                    musicService.playNextSong()
//                }
//                else -> {
//                    Log.d("SongPlayerActivity", "No action")
//                }
//            }
//        }

//        override fun onServiceDisconnected(name: ComponentName?) {
//            Log.d("SongPlayerActivity", "onServiceDisconnected")
//            isServiceBound = false
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}