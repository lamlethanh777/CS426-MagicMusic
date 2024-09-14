package com.example.cs426_magicmusic.data.source.db.synchronize

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.PlaylistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import com.example.cs426_magicmusic.others.Constants.FAVORITE_PLAYLIST_NAME
import com.example.cs426_magicmusic.others.Constants.STRING_DEFAULT_ALBUM_NAME
import com.example.cs426_magicmusic.others.Constants.STRING_DEFAULT_ARTIST_NAME
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
    private lateinit var playlistRepository: PlaylistRepository

    fun setupRepositories(
        albumRepo: AlbumRepository,
        artistRepo: ArtistRepository,
        songRepo: SongRepository,
        playlistRepo: PlaylistRepository
    ) {
        albumRepository = albumRepo
        artistRepository = artistRepo
        songRepository = songRepo
        playlistRepository = playlistRepo
    }

    private suspend fun fetchLocalMusicFiles(context: Context) {
        withContext(Dispatchers.IO) {
            // Clear existing data
            _songList.clear()
            _albumsWithSongsList.clear()
            _artistWithSongsList.clear()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION
            )

            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null
            )

            cursor?.use {
                val mediaIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(mediaIdIndex)
                    val path = cursor.getString(pathIndex) ?: continue

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId
                    )

                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)

                        val songTitle =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                ?: cursor.getString(titleIndex) ?: STRING_UNKNOWN_TITLE
                        var artistNames =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                ?: cursor.getString(artistIndex) ?: STRING_UNKNOWN_ARTIST
                        var albumName =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                                ?: cursor.getString(albumIndex) ?: STRING_UNKNOWN_ALBUM
                        val duration =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong() ?: cursor.getLong(durationIndex) ?: 0L

                        if (artistNames == STRING_UNKNOWN_ARTIST) {
                            artistNames = STRING_DEFAULT_ARTIST_NAME
                            albumName = STRING_DEFAULT_ALBUM_NAME
                        }

                        if (artistNames == STRING_SYSTEM_UNKNOWN_TAG) {
                            artistNames = STRING_UNKNOWN_ARTIST
                            Log.d("LocalDBSynchronizer", "Unknown artist for song: $songTitle")
                        }

                        Log.d(
                            "LocalDBSynchronizer",
                            "Processing song: $songTitle, path: $path, album: $albumName, artists: $artistNames, duration: $duration"
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
                        _albumsWithSongsList[album]!!.add(song)

                        // Handle multiple artists
                        val artists = artistNames.split(",").map { Artist(it.trim()) }

                        for (artist in artists) {
                            if (_artistWithSongsList[artist] == null) {
                                _artistWithSongsList[artist] = mutableListOf()
                            }
                            _artistWithSongsList[artist]?.add(song)
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e("LocalDBSynchronizer", "Could not access URI: $uri", e)
                    } finally {
                        retriever.release()
                    }
                }
            } ?: Log.e("LocalDBSynchronizer", "Cursor is null")
        }
    }

    suspend fun synchronizeDatabase(context: Context) {
        playlistRepository.insertNewPlaylist(Playlist(FAVORITE_PLAYLIST_NAME))

        Log.d("LocalDBSynchronizer", "Starting synchronization")

        // Fetch the current state of the local storage
        fetchLocalMusicFiles(context)

        Log.d("LocalDBSynchronizer", "Fetched songs from local storage: ${_songList.size}")
        Log.d(
            "LocalDBSynchronizer", "Fetched albums from local storage: ${_albumsWithSongsList.size}"
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

        // Find updated songs
        val updatedSongs = _songList.filter { song ->
            currentSongsInDb.any {
                song.path == it.path && song != it
            }
        }

        // Find newly added songs
        val newSongs = _songList.filter { song ->
            currentSongsInDb.none {
                song.path == it.path
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
        songRepository.updateSongs(updatedSongs)
        songRepository.insertAllSongs(newSongs)

        // New albums and artists
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