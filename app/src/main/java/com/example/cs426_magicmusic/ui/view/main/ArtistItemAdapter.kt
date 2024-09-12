package com.example.cs426_magicmusic.ui.view.main

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Artist

class ArtistItemAdapter(
    private val itemListener: ((Artist?) -> Unit)
) : TemplateItemAdapter<Artist>(
    itemListener,
    object : DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.artistName == newItem.artistName
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun getTitle(item: Any?): String {
        return (item as Artist).artistName
    }

    override fun getSubtitle(item: Any?): String {
        return ""
    }

    override fun getImageUri(item: Any?): String {
        return ""
    }
}