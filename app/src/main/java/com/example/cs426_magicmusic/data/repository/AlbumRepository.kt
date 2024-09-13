package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.entity.relation.albums_songs.AlbumSong
import com.example.cs426_magicmusic.data.source.db.AppDatabase

class AlbumRepository (private val appDatabase: AppDatabase) {
    suspend fun fetchAlbums(): List<Album> {
        return appDatabase.albumDao().fetchAlbums()
    }

    suspend fun fetchAlbumsOrderByName(): List<Album> {
        return appDatabase.albumDao().fetchAlbumsOrderByName()
    }

    suspend fun fetchSongsInAlbum(albumName: String): List<Song> {
        return appDatabase.albumSongDao().fetchSongsInAlbum(albumName)
    }

    suspend fun fetchSongsInAlbumOrderByTitle(albumName: String): List<Song> {
        return appDatabase.albumSongDao().fetchSongsInAlbumOrderByTitle(albumName)
    }

    suspend fun fetchSongsInAlbumOrderByArtistNames(albumName: String): List<Song> {
        return appDatabase.albumSongDao().fetchSongsInAlbumOrderByArtistNames(albumName)
    }

    suspend fun insertAllAlbumsWithSongs(albumsWithSongsList: MutableMap<Album,
            MutableList<Song>>) {
        for ((album, songs) in albumsWithSongsList) {
            appDatabase.albumDao().insert(album)
            for (song in songs) {
                val albumSong = AlbumSong(album.albumName, song.path)
                appDatabase.albumSongDao().insert(albumSong)
            }
        }
    }

    suspend fun deleteAllAlbums() {
        appDatabase.albumDao().deleteAll()
    }
}
