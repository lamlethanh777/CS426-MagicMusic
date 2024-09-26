package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.entity.relation.playlists_songs.PlaylistSong
import com.example.cs426_magicmusic.data.source.db.AppDatabase
import com.example.cs426_magicmusic.others.Constants.FAVORITE_PLAYLIST_NAME

class PlaylistRepository(private val appDatabase: AppDatabase) {
    suspend fun fetchPlaylists(): List<Playlist> {
        return appDatabase.playlistDao().fetchPlaylists()
    }

    suspend fun fetchPlaylistsOrderByName(): List<Playlist> {
        return appDatabase.playlistDao().fetchPlaylistsOrderByName()
    }

    suspend fun insertNewPlaylist(playlist: Playlist) {
        appDatabase.playlistDao().insert(playlist)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        appDatabase.playlistDao().delete(playlist)
    }

    suspend fun fetchSongsInPlaylist(playlist: Playlist): List<Song> {
        return appDatabase.playlistSongDao().fetchSongsInPlaylist(playlist.playlistName)
    }

    suspend fun fetchSongsInPlaylistOrderByTitle(playlist: Playlist): List<Song> {
        return appDatabase.playlistSongDao().fetchSongsInPlaylistOrderByTitle(playlist.playlistName)
    }

    suspend fun fetchSongsInPlaylistOrderByArtistNames(playlist: Playlist): List<Song> {
        return appDatabase.playlistSongDao()
            .fetchSongsInPlaylistOrderByArtistNames(playlist.playlistName)
    }

    suspend fun insertSongIntoPlaylist(playlist: Playlist, song: Song) {
        appDatabase.playlistSongDao().insert(PlaylistSong(playlist.playlistName, song.path))
    }

    suspend fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        appDatabase.playlistSongDao().delete(PlaylistSong(playlist.playlistName, song.path))
    }

    suspend fun addSongToFavoritePlaylist(song: Song) {
        appDatabase.playlistSongDao().insert(PlaylistSong(FAVORITE_PLAYLIST_NAME, song.path))
    }

    suspend fun deleteSongFromFavoritePlaylist(song: Song) {
        appDatabase.playlistSongDao().delete(PlaylistSong(FAVORITE_PLAYLIST_NAME, song.path))
    }

    suspend fun isSongInFavoritePlaylist(it: Song): Boolean {
        val result = appDatabase.playlistSongDao().isSongInPlaylist(FAVORITE_PLAYLIST_NAME, it.path)
        return if (result == null) {
            false
        } else {
            result > 0
        }
    }
}
