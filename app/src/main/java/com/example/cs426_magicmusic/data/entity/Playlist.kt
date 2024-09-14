package com.example.cs426_magicmusic.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "playlists")
data class Playlist(
//    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    @PrimaryKey(autoGenerate = false) val playlistName: String
) : Parcelable
