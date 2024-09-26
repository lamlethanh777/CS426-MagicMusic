package com.example.cs426_magicmusic.ui.view.main.library

import com.example.cs426_magicmusic.data.entity.Artist

class ArtistItemAdapter(
    listenerManager: ListenerManager
) : TemplateItemAdapter<Artist>(
    listenerManager
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