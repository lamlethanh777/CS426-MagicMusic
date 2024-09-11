package com.example.cs426_magicmusic.data.entity.relation.albums_songs

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Song
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "albums_songs",
        primaryKeys = ["albumName", "songPath"],
        foreignKeys = [
            ForeignKey(entity = Album::class, parentColumns = arrayOf("albumName"),
                childColumns = arrayOf("albumName"), onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Song::class, parentColumns = arrayOf("path"),
                childColumns = arrayOf("songPath"), onDelete = ForeignKey.CASCADE)
        ]
)
data class AlbumSong(
    val albumName: String,
    val songPath: String
) : Parcelable
