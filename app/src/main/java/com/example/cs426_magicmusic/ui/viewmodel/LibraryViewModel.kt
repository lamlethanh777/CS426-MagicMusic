package com.example.cs426_magicmusic.ui.viewmodel

import android.util.Log
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
import com.example.cs426_magicmusic.ui.view.main.library.TemplateItemAdapter
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _currentLayout = MutableLiveData(TemplateItemAdapter.LayoutType.LIST)
    val currentLayout: LiveData<TemplateItemAdapter.LayoutType> get() = _currentLayout

    private val _ascendingOrder = MutableLiveData(true)
    val ascendingOrder: LiveData<Boolean> get() = _ascendingOrder

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> get() = _albums

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> get() = _artists

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> get() = _playlists

    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    private val _substring = MutableLiveData("")
    val substring: LiveData<String> = _substring

    fun filterSongs(substring: String) {
        _substring.value = substring
        viewModelScope.launch {
            _filteredSongs.value = songRepository.filterSongs(substring)
        }
    }

    fun toggleCurrentOrder() {
        _ascendingOrder.value = _ascendingOrder.value!! != true
        Log.d("LibraryViewModel", "toggleCurrentOrder: ${_ascendingOrder.value}")
    }

    fun toggleCurrentLayout() {
        _currentLayout.value = when (_currentLayout.value) {
            TemplateItemAdapter.LayoutType.LIST -> TemplateItemAdapter.LayoutType.GRID
            TemplateItemAdapter.LayoutType.GRID -> TemplateItemAdapter.LayoutType.LIST
            else -> TemplateItemAdapter.LayoutType.LIST
        }
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _songs.value = songRepository.fetchSongs()
        }
    }

    fun fetchSongsOrderByTitle() {
        viewModelScope.launch {
            _songs.value = songRepository.fetchSongsOrderByTitle()
        }
    }

    fun fetchSongsOrderByArtistNames() {
        viewModelScope.launch {
            _songs.value = songRepository.fetchSongsOrderByArtistNames()
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

    fun fetchAlbumsOrderByName() {
        viewModelScope.launch {
            _albums.value = albumRepository.fetchAlbumsOrderByName()
        }
    }

    fun fetchSongsInAlbum(album: Album) {
        viewModelScope.launch {
            _songs.value = albumRepository.fetchSongsInAlbum(album.albumName)
        }
    }

    fun fetchSongsInAlbumOrderByTitle(album: Album) {
        viewModelScope.launch {
            _songs.value = albumRepository.fetchSongsInAlbumOrderByTitle(album.albumName)
        }
    }

    fun fetchSongsInAlbumOrderByArtistNames(album: Album) {
        viewModelScope.launch {
            _songs.value = albumRepository.fetchSongsInAlbumOrderByArtistNames(album.albumName)
        }
    }

    fun fetchAllArtists() {
        viewModelScope.launch {
            _artists.value = artistRepository.fetchArtists()
        }
    }

    fun fetchArtistsOrderByName() {
        viewModelScope.launch {
            _artists.value = artistRepository.fetchArtistsOrderByName()
        }
    }

    fun fetchSongsOfArtist(artist: Artist) {
        viewModelScope.launch {
            _songs.value = artistRepository.fetchSongsOfArtist(artist.artistName)
        }
    }

    fun fetchSongsOfArtistOrderByTitle(artist: Artist) {
        viewModelScope.launch {
            _songs.value = artistRepository.fetchSongsOfArtistOrderByTitle(artist.artistName)
        }
    }

    fun fetchAllPlaylists() {
        viewModelScope.launch {
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun fetchPlaylistsOrderByName() {
        viewModelScope.launch {
            _playlists.value = playlistRepository.fetchPlaylistsOrderByName()
        }
    }

    fun fetchSongsInPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            _songs.value = playlistRepository.fetchSongsInPlaylist(playlist)
        }
    }

    fun fetchSongsInPlaylistOrderByTitle(playlist: Playlist) {
        viewModelScope.launch {
            _songs.value = playlistRepository.fetchSongsInPlaylistOrderByTitle(playlist)
        }
    }

    fun fetchSongsInPlaylistOrderByArtistNames(playlist: Playlist) {
        viewModelScope.launch {
            _songs.value = playlistRepository.fetchSongsInPlaylistOrderByArtistNames(playlist)
        }
    }

    fun addNewPlaylist(playlistName: String) {
        viewModelScope.launch {
            playlistRepository.insertNewPlaylist(Playlist(playlistName = playlistName))
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            playlistRepository.insertSongIntoPlaylist(playlist, song)
            _playlists.value = playlistRepository.fetchPlaylists()
        }
    }

    fun addMultipleSongsToPlaylist(playlist: Playlist, songs: List<Song>) {
        viewModelScope.launch {
            songs.forEach { song ->
                playlistRepository.insertSongIntoPlaylist(playlist, song)
            }
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
