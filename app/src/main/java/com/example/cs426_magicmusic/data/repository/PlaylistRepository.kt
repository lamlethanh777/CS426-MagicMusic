package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.source.db.AppDatabase

class PlaylistRepository (private val appDatabase: AppDatabase) {
    suspend fun fetchPlaylists(): List<Playlist> {
        return appDatabase.playlistDao().fetchPlaylists()
    }

    suspend fun fetchPlaylistsOrderByName(): List<Playlist> {
        return appDatabase.playlistDao().fetchPlaylistsOrderByName()
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
        return appDatabase.playlistSongDao().fetchSongsInPlaylistOrderByArtistNames(playlist.playlistName)
    }

    suspend fun insertSongIntoPlaylist(playlist: Playlist, song: Song) {
        appDatabase.playlistSongDao().insertSongIntoPlaylist(playlist.playlistName, song.path)
    }

    suspend fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        appDatabase.playlistSongDao().removeSongFromPlaylist(playlist.playlistName, song.path)
    }
}
