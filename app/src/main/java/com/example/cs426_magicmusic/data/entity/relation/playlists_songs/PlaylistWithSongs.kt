package com.example.cs426_magicmusic.data.entity.relation.playlists_songs

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song

data class PlaylistWithSongs(
    @Embedded val playlist: Playlist,
    @Relation(
        parentColumn = "playlistName",
        entityColumn = "songPath",
        associateBy = Junction(PlaylistSong::class)
    )
    val songs: List<Song>
)
