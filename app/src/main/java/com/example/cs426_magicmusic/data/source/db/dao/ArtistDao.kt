package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.cs426_magicmusic.data.entity.Artist

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg artists: Artist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artist: Artist)

    @Upsert
    suspend fun upsert(artist: Artist)

    @Delete
    suspend fun delete(vararg artists: Artist)

    @Query("DELETE FROM artists")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM artists")
    suspend fun fetchArtists(): List<Artist>
}
