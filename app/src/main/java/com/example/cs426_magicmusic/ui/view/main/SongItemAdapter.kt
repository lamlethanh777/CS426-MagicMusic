package com.example.cs426_magicmusic.ui.view.main

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Song

class SongItemAdapter(
    private val itemListener: ((Song?) -> Unit)
) : TemplateItemAdapter<Song>(
    itemListener,
    object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun getTitle(item: Any?): String {
        return (item as Song).title
    }

    override fun getSubtitle(item: Any?): String {
        return (item as Song).artistNames
    }

    override fun getImageUri(item: Any?): String {
        return (item as Song).uri
    }
}