package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.cs426_magicmusic.data.entity.relation.artists_songs.ArtistSong
import com.example.cs426_magicmusic.data.entity.Song

@Dao
interface ArtistSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg artistSongs: ArtistSong)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artistSong: ArtistSong)

    @Upsert
    suspend fun upsert(artistSong: ArtistSong)

    @Delete
    suspend fun delete(vararg artistSongs: ArtistSong)

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN artists_songs ON songs.path = artists_songs.songPath" +
                " WHERE artists_songs.artistName = :artistName"
    )
    suspend fun fetchSongsOfArtist(artistName: String): List<Song>
}
