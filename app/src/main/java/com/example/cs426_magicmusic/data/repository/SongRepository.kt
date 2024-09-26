package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.source.db.AppDatabase

class SongRepository (private val appDatabase: AppDatabase) {
    suspend fun fetchSongs(): List<Song> {
        return appDatabase.songDao().fetchSongs()
    }

    suspend fun fetchSongsOrderByTitle(): List<Song> {
        return appDatabase.songDao().fetchSongsOrderByTitle()
    }

    suspend fun fetchSongsOrderByArtistNames(): List<Song> {
        return appDatabase.songDao().fetchSongsOrderByArtistNames()
    }

    suspend fun insertAllSongs(songs: List<Song>) {
        appDatabase.songDao().insertAll(*songs.map { it }.toTypedArray())
    }

    suspend fun updateSong(song: Song) {
        appDatabase.songDao().update(song)
    }

    suspend fun updateSongs(songs: List<Song>) {
        appDatabase.songDao().updateAll(songs)
    }

    suspend fun upsertSong(song: Song) {
        appDatabase.songDao().upsert(song)
    }

    suspend fun deleteSong(song: Song) {
        appDatabase.songDao().delete(song)
    }

    suspend fun deleteSongs(songs: List<Song>) {
        appDatabase.songDao().delete(*songs.map { it }.toTypedArray())
    }

    suspend fun filterSongs(substring: String): List<Song> {
        val query = "%$substring%"
        return appDatabase.songDao().filterSongs(query)
    }
}
