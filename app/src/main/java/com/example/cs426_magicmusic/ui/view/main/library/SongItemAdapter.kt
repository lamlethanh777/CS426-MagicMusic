package com.example.cs426_magicmusic.ui.view.main.library

import androidx.recyclerview.widget.DiffUtil
import com.example.cs426_magicmusic.data.entity.Song

class SongItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Song>(
    listenerManager
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