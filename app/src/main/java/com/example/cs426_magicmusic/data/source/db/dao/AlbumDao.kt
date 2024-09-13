package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.cs426_magicmusic.data.entity.Album

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg albums: Album)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: Album)

    @Upsert
    suspend fun upsert(album: Album)

    @Delete
    suspend fun delete(vararg albums: Album)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM albums")
    suspend fun fetchAlbums(): List<Album>

    @Transaction
    @Query("SELECT * FROM albums ORDER BY albumName")
    suspend fun fetchAlbumsOrderByName(): List<Album>
}
