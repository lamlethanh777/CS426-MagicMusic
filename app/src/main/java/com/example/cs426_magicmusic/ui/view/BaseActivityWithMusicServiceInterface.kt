package com.example.cs426_magicmusic.ui.view

/**
 * Interface for activities that need to interact with the music service
 * As SongPlayerActivity and MainActivity are almost the same
 * NOT IMPLEMENTED YET
 */
interface BaseActivityWithMusicServiceInterface {
    fun onServiceConnected()
    fun onServiceDisconnected()
}