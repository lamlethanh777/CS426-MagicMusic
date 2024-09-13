package com.example.cs426_magicmusic.ui.view.main.library

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Playlist

class PlaylistItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Playlist>(
    listenerManager,
    object : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.playlistName == newItem.playlistName
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
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