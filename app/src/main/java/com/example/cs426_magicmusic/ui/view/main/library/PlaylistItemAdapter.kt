package com.example.cs426_magicmusic.ui.view.main.library

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Playlist

class PlaylistItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Playlist>(
    listenerManager
) {
    override fun getTitle(item: Any?): String {
        return (item as Playlist).playlistName
    }

    override fun getSubtitle(item: Any?): String {
        return ""
    }

    override fun getImageUri(item: Any?): String {
        return ""
    }
}