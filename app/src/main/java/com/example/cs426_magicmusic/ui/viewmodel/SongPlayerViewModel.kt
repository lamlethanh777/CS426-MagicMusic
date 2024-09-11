package com.example.cs426_magicmusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants
import com.example.cs426_magicmusic.service.musicplayer.MusicPlayerService
import java.lang.ref.WeakReference

class SongPlayerViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song>  = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentSongDuration = MutableLiveData<Int>()
    val songDuration: LiveData<Int> = _currentSongDuration

    private val _currentSongPosition = MutableLiveData<Int>()
    val currentSongPosition: LiveData<Int> = _currentSongPosition

    private val _playerMode = MutableLiveData<Constants.PlayerMode>()
    val playerMode: LiveData<Constants.PlayerMode> = _playerMode

    // WeakReference to avoid memory leaks
    private var musicPlayerServiceRef: WeakReference<MusicPlayerService>? = null

    fun setCurrentSong(song: Song) {
        _currentSong.value = song
    }

    fun setIsPlaying() {
        _isPlaying.value = !(isPlaying.value?: false)
        if (isPlaying.value == true) {
            musicPlayerServiceRef?.get()?.resume()
        } else {
            musicPlayerServiceRef?.get()?.pauseSong()
        }
    }

    fun setSongDuration(duration: Int) {
        _currentSongDuration.value = duration
    }

    // Store the observer so that we can remove it later
    private var currentSongObserver: Observer<Song>? = null
    private var isPlayingObserver: Observer<Boolean>? = null
    private var currentPositionObserver: Observer<Int>? = null

    // Function to set the MusicPlayerService and observe its LiveData
    fun setMusicService(musicPlayerService: MusicPlayerService) {
        musicPlayerServiceRef = WeakReference(musicPlayerService)

        currentSongObserver = Observer { song ->
            _currentSong.value = song
        }
        isPlayingObserver = Observer {
            _isPlaying.value = it
        }
        currentPositionObserver = Observer {
            _currentSongPosition.value = it
        }

        musicPlayerService.currentSongLiveData.observeForever(currentSongObserver!!)
        musicPlayerService.isPlayingLiveData.observeForever(isPlayingObserver!!)
        musicPlayerService.currentSongPositionLiveData.observeForever(currentPositionObserver!!)
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerServiceRef?.get()?.let { musicService ->
            musicService.currentSongLiveData.removeObserver(currentSongObserver!!)
            musicService.isPlayingLiveData.removeObserver(isPlayingObserver!!)
            musicService.currentSongPositionLiveData.removeObserver(currentPositionObserver!!)
        }
    }

    fun setCurrentPlayerPosition(position: Int) {
        musicPlayerServiceRef?.get()?.let { musicService ->
            musicService.setCurrentPosition(position)
            if (isPlaying.value == true) {
                musicService.resume()
            }
        }
    }

    fun setPlayerMode(mode: Constants.PlayerMode) {
        _playerMode.value = mode
    }

    fun setCurrentSong(musicPlayerService: MusicPlayerService?) {
        _currentSong.value = musicPlayerService?.getCurrentSong()
    }
}