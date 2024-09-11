package com.example.cs426_magicmusic.data.entity.relation.albums_songs

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Song

data class AlbumWithSongs(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "albumName",
        entityColumn = "songPath",
        associateBy = Junction(AlbumSong::class)
    )
    val songs: List<Song>
)
