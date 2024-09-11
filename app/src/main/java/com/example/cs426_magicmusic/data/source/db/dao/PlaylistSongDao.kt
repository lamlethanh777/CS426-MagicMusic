package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cs426_magicmusic.data.entity.relation.playlists_songs.PlaylistSong
import com.example.cs426_magicmusic.data.entity.Song

@Dao
interface PlaylistSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg playlistSongs: PlaylistSong)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlistSong: PlaylistSong)

    @Transaction
    @Query("INSERT INTO playlists_songs" +
            " VALUES (:playlistName, :songPath)")
    suspend fun insertSongIntoPlaylist(playlistName: String, songPath: String)

    @Delete
    suspend fun delete(playlistSong: PlaylistSong)

    @Transaction
    @Query("DELETE FROM playlists_songs " +
            " WHERE songPath = :songPath AND playlistName = :playlistName")
    suspend fun removeSongFromPlaylist(songPath: String, playlistName: String)

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN playlists_songs ON songs.path = playlists_songs.songPath " +
                " WHERE playlists_songs.playlistName = :playlistName"
    )
    suspend fun fetchSongsInPlaylist(playlistName: String): List<Song>
}
