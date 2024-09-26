package com.example.cs426_magicmusic.ui.view.main.library

import com.example.cs426_magicmusic.data.entity.Album

class AlbumItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Album>(
    listenerManager
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