package com.example.cs426_magicmusic.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants
import com.example.cs426_magicmusic.service.musicplayer.MusicPlayerService
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MainViewModel : ViewModel() {

    private var currentJob = viewModelScope.launch{}

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
            Log.d("Next song", song.toString())
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
            currentSongObserver?.let { musicService.currentSongLiveData.removeObserver(it) }
            isPlayingObserver?.let { musicService.isPlayingLiveData.removeObserver(it) }
            currentPositionObserver?.let { musicService.currentSongPositionLiveData.removeObserver(it) }
        }
    }

    fun setCurrentPlayerPosition(position: Int) {
        _currentSongPosition.value = position
//        currentJob.cancel()
//        _currentSongPosition.value = position
//        musicPlayerServiceRef?.get()?.setCurrentPosition(position)
//
//        currentJob = viewModelScope.launch {
//            while (isActive) {
//                val position = musicPlayerServiceRef?.get()?.getCurrentPosition()
//                if (position != null && currentSongPosition.value != position) {
//                    _currentSongPosition.postValue(position!!)
//                }
//                delay(UPDATE_PLAYER_POSITION_INTERVAL)
//            }
//        }
    }

    fun setPlayerMode(mode: Constants.PlayerMode) {
        (playerMode as MutableLiveData).value = mode
    }

    fun setCurrentSong(musicPlayerService: MusicPlayerService?) {
        val song = musicPlayerService?.getCurrentSong()
        if (song != null) {
            _currentSong.value = song!!
        }
    }
}