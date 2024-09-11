package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.cs426_magicmusic.data.entity.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg songs: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: Song)

    @Update
    suspend fun update(song: Song)

    @Update
    suspend fun updateAll(songs: List<Song>)

    @Upsert
    suspend fun upsert(song: Song)

    @Delete
    suspend fun delete(vararg songs: Song)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM songs")
    suspend fun fetchSongs(): List<Song>

    @Transaction
    @Query("SELECT * FROM songs where title = :songTitle")
    suspend fun fetchSongByTitle(songTitle: String): Song?

    @Transaction
    @Query("SELECT * FROM songs where path = :path")
    suspend fun fetchSongByPath(path: String): Song?
}
