package com.example.cs426_magicmusic.data.repository

import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.source.db.AppDatabase

class SongRepository (private val appDatabase: AppDatabase) {
    suspend fun fetchSongs(): List<Song> {
        return appDatabase.songDao().fetchSongs()
    }

    suspend fun fetchSongByTitle(songTitle: String): Song? {
        return appDatabase.songDao().fetchSongByTitle(songTitle)
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

    suspend fun insertSong(song: Song) {
        appDatabase.songDao().insert(song)
    }

//    suspend fun updateSong(oldSongData: Song, newSongData: Song) {
//        val newLyricPath = null
//        if (song.lyricPath != null) {
//            // function to create new lyric path
//            val newLyricPath = song.lyricPath.replace(song.title, newTitle)
//        }
//        val newSong = song.copy(title = new, lyricPath = newLyricPath)
//
//        appDatabase.songDao().de
//        appDatabase.songDao().update(newSong)
//    }

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

    suspend fun deleteAllSongs() {
        appDatabase.songDao().deleteAll()
    }

    suspend fun deleteSongs(songs: List<Song>) {
        appDatabase.songDao().delete(*songs.map { it }.toTypedArray())
    }
}
