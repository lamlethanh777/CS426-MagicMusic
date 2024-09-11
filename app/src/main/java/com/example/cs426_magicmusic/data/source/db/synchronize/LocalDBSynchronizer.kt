package com.example.cs426_magicmusic.data.source.db.synchronize

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalDBSynchronizer {
    private val _songList = mutableListOf<Song>()
    private val _albumsWithSongsList = HashMap<Album, MutableList<Song>>()
    private val _artistWithSongsList = HashMap<Artist, MutableList<Song>>()

    private lateinit var albumRepository: AlbumRepository
    private lateinit var artistRepository: ArtistRepository
    private lateinit var songRepository: SongRepository

    fun setupRepositories(
        albumRepo: AlbumRepository,
        artistRepo: ArtistRepository,
        songRepo: SongRepository
    ) {
        albumRepository = albumRepo
        artistRepository = artistRepo
        songRepository = songRepo
    }

    private suspend fun fetchLocalMusicFiles(context: Context) {
        withContext(Dispatchers.IO) {
            // Clear existing data
            _songList.clear()
            _albumsWithSongsList.clear()
            _artistWithSongsList.clear()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.GENRE
            )

            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )

            cursor?.use {
                val mediaIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(mediaIdIndex)
                    val albumId = cursor.getString(albumIdIndex)
                    val songTitle = cursor.getString(titleIndex)
                    val path = cursor.getString(pathIndex)
                    val duration = cursor.getLong(durationIndex)
                    val artistNames = cursor.getString(artistIndex)
                        .let { v -> if (v == "<unknown>") null else v }
                    val albumName = cursor.getString(albumIndex)

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )

                    val song = Song(
                        path = path,
                        title = songTitle,
                        uri = uri.toString(),
                        duration = duration,
                        artistNames = artistNames
                    )
                    _songList.add(song)

                    val album = Album(albumName = albumName)

                    if (_albumsWithSongsList[album] == null) {
                        _albumsWithSongsList[album] = mutableListOf()
                    }
                    _albumsWithSongsList[album]?.add(song)

                    // Handle multiple artists
                    val artists = artistNames?.split(",")
                        ?.map { Artist(it.trim()) }
                        ?: emptyList()

                    for (artist in artists) {
                        if (_artistWithSongsList[artist] == null) {
                            _artistWithSongsList[artist] = mutableListOf()
                        }
                        _artistWithSongsList[artist]?.add(song)
                    }
                }
            }
        }
    }

    suspend fun synchronizeDatabase(context: Context) {
        Log.d("LocalDBSynchronizer", "Starting synchronization")

        fetchLocalMusicFiles(context)

        Log.d("LocalDBSynchronizer", "Fetched songs: ${_songList.size}")
        Log.d("LocalDBSynchronizer", "Fetched albums: ${_albumsWithSongsList.size}")
        Log.d("LocalDBSynchronizer", "Fetched artists: ${_artistWithSongsList.size}")

        // Delete all existing data
        songRepository.deleteAllSongs()
        albumRepository.deleteAllAlbums()
        artistRepository.deleteAllArtists()

        // Insert new data
        songRepository.insertAllSongs(_songList)
        albumRepository.insertAllAlbumsWithSongs(_albumsWithSongsList.toMutableMap())
        artistRepository.insertAllArtistsWithSongs(_artistWithSongsList.toMutableMap())

        // Log the final state of the database
        Log.d("LocalDBSynchronizer", "Songs after sync: ${songRepository.fetchSongs().size}")
    }
}