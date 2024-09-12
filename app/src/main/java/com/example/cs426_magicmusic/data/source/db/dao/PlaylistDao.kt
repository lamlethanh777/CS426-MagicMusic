package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.cs426_magicmusic.data.entity.Playlist

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg playlists: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: Playlist)

    @Update
    suspend fun update(playlist: Playlist)

    @Upsert
    suspend fun upsert(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)

    @Transaction
    @Query("SELECT * FROM playlists")
    suspend fun fetchPlaylists(): List<Playlist>

    @Transaction
    @Query("SELECT * FROM playlists ORDER BY playlistName")
    suspend fun fetchPlaylistsOrderByName(): List<Playlist>
}
