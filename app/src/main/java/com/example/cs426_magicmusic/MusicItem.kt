package com.example.cs426_magicmusic

import java.io.Serializable

class MusicItem (
    private val _title: String,
    private val _author: List<String>,
    private val _icon: Int,
    private val _audioId: Int,
    private val _musicId: Int
) : Serializable {
    val title: String
        get() = _title

    val author: List<String>
        get() = _author

    val icon: Int
        get() = _icon

    val audioId: Int
        get() = _audioId

    val musicId: Int
        get() = _musicId
}
