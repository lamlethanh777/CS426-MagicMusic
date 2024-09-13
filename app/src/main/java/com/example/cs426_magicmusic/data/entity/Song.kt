package com.example.cs426_magicmusic.data.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = false) val path: String,
    val title: String,
    val uri: String,
    val duration: Long,
    val artistNames: String,
    val isFavorite: Boolean = false
) : Parcelable
