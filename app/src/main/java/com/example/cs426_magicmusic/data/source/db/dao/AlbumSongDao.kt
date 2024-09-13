package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cs426_magicmusic.data.entity.relation.albums_songs.AlbumSong
import com.example.cs426_magicmusic.data.entity.Song

@Dao
interface AlbumSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg albumSongs: AlbumSong)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(albumSong: AlbumSong)

    @Delete
    suspend fun delete(vararg albumSongs: AlbumSong)

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN albums_songs ON songs.path = albums_songs.songPath" +
                " WHERE albums_songs.albumName = :albumName"
    )
    suspend fun fetchSongsInAlbum(albumName: String): List<Song>

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN albums_songs ON songs.path = albums_songs.songPath" +
                " WHERE albums_songs.albumName = :albumName" +
                " ORDER BY songs.title"
    )
    suspend fun fetchSongsInAlbumOrderByTitle(albumName: String): List<Song>

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN albums_songs ON songs.path = albums_songs.songPath" +
                " WHERE albums_songs.albumName = :albumName" +
                " ORDER BY songs.artistNames"
    )
    suspend fun fetchSongsInAlbumOrderByArtistNames(albumName: String): List<Song>
}
