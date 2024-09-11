package com.example.cs426_magicmusic.data.entity.relation.playlists_songs

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "playlists_songs",
        primaryKeys = ["playlistName", "songPath"],
        foreignKeys = [
            ForeignKey(entity = Playlist::class, parentColumns = arrayOf("playlistName"),
                childColumns = arrayOf("playlistName"), onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Song::class, parentColumns = arrayOf("path"),
                childColumns = arrayOf("songPath"), onDelete = ForeignKey.CASCADE)
        ]
)
data class PlaylistSong(
    val playlistName: String,
    val songPath: String
) : Parcelable
