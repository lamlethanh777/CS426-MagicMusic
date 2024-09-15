package com.example.cs426_magicmusic.data.entity.relation.artists_songs

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "artists_songs",
        primaryKeys = ["artistName", "songPath"],
        foreignKeys = [
            ForeignKey(entity = Artist::class, parentColumns = arrayOf("artistName"),
            childColumns = arrayOf("artistName"), onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = Song::class, parentColumns = arrayOf("path"),
            childColumns = arrayOf("songPath"), onDelete = ForeignKey.CASCADE)
        ]
)
data class ArtistSong(
    val artistName: String,
    val songPath: String
) : Parcelable
