package com.example.cs426_magicmusic.data.entity.relation.artists_songs

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song

//data class SongWithArtists(
//    @Embedded val song: Song,
//    @Relation(
//        parentColumn = "songId",
//        entityColumn = "artistId",
//        associateBy = Junction(ArtistSong::class)
//    )
//    val artists: List<Artist>
//)

data class SongWithArtists(
    @Embedded val song: Song,
    @Relation(
        parentColumn = "songPath",
        entityColumn = "artistName",
        associateBy = Junction(ArtistSong::class)
    )
    val artists: List<Artist>
)
