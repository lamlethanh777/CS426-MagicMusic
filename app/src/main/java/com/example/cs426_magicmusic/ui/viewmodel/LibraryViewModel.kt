package com.example.cs426_magicmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.PlaylistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    fun fetchSongs() {
        viewModelScope.launch {
            _songs.value = songRepository.fetchSongs()
        }
    }

    fun updateSong(song: Song) {
        viewModelScope.launch {
            songRepository.upsertSong(song)
            _songs.value = songRepository.fetchSongs()
        }
    }

    fun addSong(song: Song) {
        viewModelScope.launch {
            songRepository.upsertSong(song)
            _songs.value = songRepository.fetchSongs()
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songRepository.deleteSong(song)
            _songs.value = songRepository.fetchSongs()
        }
    }

    fun fetchAllAlbums() {
        viewModelScope.launch {
            _albums.value = albumRepository.fetchAlbums()
        }
    }

    fun fetchAllArtists() {
        viewModelScope.launch {
            _artists.value = artistRepository.fetchArtists()
        }
    }

    fun fetchAllPlaylists() {
        viewModelScope.launch {
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            playlistRepository.insertSongIntoPlaylist(playlist, song)
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlist, song)
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }
}
