package com.example.cs426_magicmusic.data.entity.relation.artists_songs

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song

data class ArtistWithSongs(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "artistName",
        entityColumn = "songPath",
        associateBy = Junction(ArtistSong::class)
    )
    val songs: List<Song>
)
