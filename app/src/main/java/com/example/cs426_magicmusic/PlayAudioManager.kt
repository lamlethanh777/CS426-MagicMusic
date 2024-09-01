package com.example.cs426_magicmusic

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer

@SuppressLint("StaticFieldLeak")
object PlayAudioManager {

    private var mediaPlayer: MediaPlayer? = null
    private var musicStarted = false

    private lateinit var context: Context
    fun init(context: Context) {
        this.context = context
    }

    fun setResource(resId: Int) {
        mediaPlayer?.release() // Release old resources if any
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.setOnCompletionListener { it.start() }
        musicStarted = false
    }

    fun play() {
        mediaPlayer?.start()
        musicStarted = true
    }

    fun pause() {
        if (musicStarted) mediaPlayer?.pause()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun getAudioLong(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentTimestamp(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun setCurrentTimestamp(timestamp: Int) {
        var safeTimestamp = if (timestamp < 0) 0 else timestamp
        if (safeTimestamp > getAudioLong()) {
            safeTimestamp = getAudioLong()
        }
        mediaPlayer?.seekTo(safeTimestamp)
    }
}