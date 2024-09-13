package com.example.cs426_magicmusic.data.source.db.synchronize

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import com.example.cs426_magicmusic.others.Constants.STRING_SYSTEM_UNKNOWN_TAG
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_ALBUM
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_ARTIST
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_TITLE
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
//                val albumIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(mediaIdIndex)
//                    val albumId = cursor.getString(albumIdIndex)
                    var songTitle = cursor.getString(titleIndex)
                    val path = cursor.getString(pathIndex)
                    val duration = cursor.getLong(durationIndex)
                    var artistNames =
                        cursor.getString(artistIndex)?.takeIf { it != STRING_SYSTEM_UNKNOWN_TAG }
                    var albumName = cursor.getString(albumIndex)

                    Log.d(
                        "LocalDBSynchronizer",
                        "Processing song: $songTitle, path: $path, album: $albumName, artists: $artistNames"
                    )

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    )

                    if (path == null) {
                        continue
                    }

                    songTitle = songTitle ?: STRING_UNKNOWN_TITLE
                    artistNames = artistNames ?: STRING_UNKNOWN_ARTIST
                    albumName = albumName ?: STRING_UNKNOWN_ALBUM

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
                    val artists = artistNames.split(",").map { Artist(it.trim()) }

                    for (artist in artists) {
                        if (_artistWithSongsList[artist] == null) {
                            _artistWithSongsList[artist] = mutableListOf()
                        }
                        _artistWithSongsList[artist]?.add(song)
                    }
                }
            } ?: Log.e("LocalDBSynchronizer", "Cursor is null")
        }
    }

    suspend fun synchronizeDatabase(context: Context) {
        Log.d("LocalDBSynchronizer", "Starting synchronization")

        // Fetch the current state of the local storage
        fetchLocalMusicFiles(context)

        Log.d("LocalDBSynchronizer", "Fetched songs from local storage: ${_songList.size}")
        Log.d(
            "LocalDBSynchronizer",
            "Fetched albums from local storage: ${_albumsWithSongsList.size}"
        )
        Log.d(
            "LocalDBSynchronizer",
            "Fetched artists from local storage: ${_artistWithSongsList.size}"
        )

        // Fetch the current state of the database
        val currentSongsInDb = songRepository.fetchSongs()
        val currentAlbumsInDb = albumRepository.fetchAlbums()
        val currentArtistsInDb = artistRepository.fetchArtists()

        Log.d("LocalDBSynchronizer", "Current songs in DB: ${currentSongsInDb.size}")
        Log.d("LocalDBSynchronizer", "Current albums in DB: ${currentAlbumsInDb.size}")
        Log.d("LocalDBSynchronizer", "Current artists in DB: ${currentArtistsInDb.size}")

        // Find newly added songs
        val newSongs = _songList.filter { song ->
            currentSongsInDb.none {
                it.path == song.path && it.duration == song.duration
            }
        }

        // Find deleted songs
        val deletedSongs = currentSongsInDb.filter { song ->
            _songList.none { it.path == song.path }
        }

        Log.d("LocalDBSynchronizer", "New songs to add: ${newSongs.size}")
        Log.d("LocalDBSynchronizer", "Songs to delete: ${deletedSongs.size}")

        // Update the database
        songRepository.deleteSongs(deletedSongs)
        songRepository.insertAllSongs(newSongs)

        // Update albums and artists
        val newAlbums = _albumsWithSongsList.keys.filter { album ->
            currentAlbumsInDb.none { it.albumName == album.albumName }
        }
        val newArtists = _artistWithSongsList.keys.filter { artist ->
            currentArtistsInDb.none { it.artistName == artist.artistName }
        }

        Log.d("LocalDBSynchronizer", "New albums to add: ${newAlbums.size}")
        Log.d("LocalDBSynchronizer", "New artists to add: ${newArtists.size}")

        // Insert new albums and artists
        albumRepository.insertAllAlbumsWithSongs(newAlbums.associateWith { _albumsWithSongsList[it]!! }
            .toMutableMap())
        artistRepository.insertAllArtistsWithSongs(newArtists.associateWith { _artistWithSongsList[it]!! }
            .toMutableMap())

        // Log the final state of the database
        Log.d("LocalDBSynchronizer", "Songs after sync: ${songRepository.fetchSongs().size}")
        Log.d("LocalDBSynchronizer", "Albums after sync: ${albumRepository.fetchAlbums().size}")
        Log.d("LocalDBSynchronizer", "Artists after sync: ${artistRepository.fetchArtists().size}")
    }
}