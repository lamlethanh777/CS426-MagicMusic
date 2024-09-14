package com.example.cs426_magicmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.PlaylistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import kotlinx.coroutines.launch

class SearchViewModel(
    private val songRepository: SongRepository,
    albumRepository: AlbumRepository,
    artistRepository: ArtistRepository,
    playlistRepository: PlaylistRepository
) : ViewModel() {
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
}