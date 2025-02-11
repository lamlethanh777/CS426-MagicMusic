package com.example.cs426_magicmusic.data.source.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.entity.relation.playlists_songs.PlaylistSong

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

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN playlists_songs ON songs.path = playlists_songs.songPath " +
                " WHERE playlists_songs.playlistName = :playlistName" +
                " ORDER BY songs.title"
    )
    suspend fun fetchSongsInPlaylistOrderByTitle(playlistName: String): List<Song>

    @Transaction
    @Query(
        "SELECT * FROM songs" +
                " INNER JOIN playlists_songs ON songs.path = playlists_songs.songPath " +
                " WHERE playlists_songs.playlistName = :playlistName" +
                " ORDER BY songs.artistNames"
    )
    suspend fun fetchSongsInPlaylistOrderByArtistNames(playlistName: String): List<Song>

    @Query(
        "SELECT COUNT(*) FROM playlists_songs" +
                " WHERE playlists_songs.playlistName = :favoritePlaylistName AND songPath = :path"
    )
    suspend fun isSongInPlaylist(favoritePlaylistName: String, path: String): Int?
}
