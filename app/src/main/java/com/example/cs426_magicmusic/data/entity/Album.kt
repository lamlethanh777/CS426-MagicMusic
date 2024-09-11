package com.example.cs426_magicmusic.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "albums")
data class Album(
//    @PrimaryKey(autoGenerate = true) val albumId: Long = 0,
    @PrimaryKey(autoGenerate = false) val albumName: String
) : Parcelable
