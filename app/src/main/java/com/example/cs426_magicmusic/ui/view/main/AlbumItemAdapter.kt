package com.example.cs426_magicmusic.ui.view.main

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Album

class AlbumItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Album>(
    listenerManager,
    object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.albumName == newItem.albumName
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun getTitle(item: Any?): String {
        return (item as Album).albumName
    }

    override fun getSubtitle(item: Any?): String {
        return ""
    }

    override fun getImageUri(item: Any?): String {
        return ""
    }
}