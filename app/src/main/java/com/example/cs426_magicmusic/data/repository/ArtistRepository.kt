package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.entity.relation.artists_songs.ArtistSong
import com.example.cs426_magicmusic.data.source.db.AppDatabase

class ArtistRepository (private val appDatabase: AppDatabase) {
    suspend fun fetchArtists(): List<Artist> {
        return appDatabase.artistDao().fetchArtists()
    }

    suspend fun fetchArtistsOrderByName(): List<Artist> {
        return appDatabase.artistDao().fetchArtistsOrderByName()
    }

    suspend fun fetchSongsOfArtist(artistName: String): List<Song> {
        return appDatabase.artistSongDao().fetchSongsOfArtist(artistName)
    }

    suspend fun fetchSongsOfArtistOrderByTitle(artistName: String): List<Song> {
        return appDatabase.artistSongDao().fetchSongsOfArtistOrderByTitle(artistName)
    }

    suspend fun insertAllArtistsWithSongs(artistWithSongsList: MutableMap<Artist,
            MutableList<Song>>) {
        for ((artist, songs) in artistWithSongsList) {
            appDatabase.artistDao().insert(artist)
            for (song in songs) {
                val artistSong = ArtistSong(artist.artistName, song.path)
                appDatabase.artistSongDao().insert(artistSong)
            }
        }
    }
}
