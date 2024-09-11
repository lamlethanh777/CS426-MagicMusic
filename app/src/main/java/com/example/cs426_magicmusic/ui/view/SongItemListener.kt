package com.example.cs426_magicmusic.ui.view

import com.example.cs426_magicmusic.data.entity.Song

/**
 * Interface for song item listener, in case an item is clicked, swipe, etc.
 * NOT IMPLEMENTED YET
 */
interface SongItemListener {
    fun songItemClicked(song: Song)
}