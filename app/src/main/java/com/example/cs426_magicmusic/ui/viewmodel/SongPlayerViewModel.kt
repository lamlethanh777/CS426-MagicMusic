package com.example.cs426_magicmusic.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_NONE
import com.example.cs426_magicmusic.others.Constants.PLAYER_SHUFFLE_MODE_OFF
import com.example.cs426_magicmusic.others.Constants.NEXT_SONG
import com.example.cs426_magicmusic.others.Constants.PREVIOUS_SONG
import com.example.cs426_magicmusic.service.musicplayer.MusicPlayerService
import com.example.cs426_magicmusic.utils.LyricUtility
import java.lang.ref.WeakReference

class SongPlayerViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying
    private val _currentSongPosition = MutableLiveData<Int>()
    val currentSongPosition: LiveData<Int> = _currentSongPosition
    private val _repeatMode = MutableLiveData(PLAYER_REPEAT_MODE_NONE)
    val repeatMode: LiveData<Int> = _repeatMode
    private val _shuffleMode = MutableLiveData(PLAYER_SHUFFLE_MODE_OFF)
    val shuffleMode: LiveData<Boolean> = _shuffleMode

    private var isLyricLoaded = false
    private var lyricText = "No lyric available"

    // WeakReference to avoid memory leaks
    private var musicPlayerServiceRef: WeakReference<MusicPlayerService>? = null

    fun setIsPlaying() {
        _isPlaying.value = !(isPlaying.value ?: false)
        if (isPlaying.value == true) {
            musicPlayerServiceRef?.get()?.resume()
        } else {
            musicPlayerServiceRef?.get()?.pause()
        }
    }

    private var currentSongObserver: Observer<Song?>? = null
    private var isPlayingObserver: Observer<Boolean>? = null
    private var currentPositionObserver: Observer<Int>? = null
    private var repeatModeObserver: Observer<Int>? = null
    private var shuffleModeObserver: Observer<Boolean>? = null

    fun setMusicService(musicPlayerService: MusicPlayerService) {
        musicPlayerServiceRef = WeakReference(musicPlayerService)

        currentSongObserver = Observer { song ->
            song?.let {
                Log.d("SongPlayerViewModel", it.toString())
                _currentSong.value = it
            } ?: run {
                Log.e("SongPlayerViewModel", "Current song is null")
            }
        }
        isPlayingObserver = Observer {
            _isPlaying.value = it
            Log.d("SongPlayerActivity", "isPlaying: $it")
        }
        currentPositionObserver = Observer {
            _currentSongPosition.value = it
        }
        repeatModeObserver = Observer {
            _repeatMode.value = it
        }
        shuffleModeObserver = Observer {
            _shuffleMode.value = it
        }

        musicPlayerService.currentSongLiveData.observeForever(currentSongObserver!!)
        musicPlayerService.isPlayingLiveData.observeForever(isPlayingObserver!!)
        musicPlayerService.currentSongPositionLiveData.observeForever(currentPositionObserver!!)
        musicPlayerService.repeatModeLiveData.observeForever(repeatModeObserver!!)
        musicPlayerService.shuffleModeLiveData.observeForever(shuffleModeObserver!!)
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerServiceRef?.get()?.let { musicService ->
            musicService.currentSongLiveData.removeObserver(currentSongObserver!!)
            musicService.isPlayingLiveData.removeObserver(isPlayingObserver!!)
            musicService.currentSongPositionLiveData.removeObserver(currentPositionObserver!!)
            musicService.repeatModeLiveData.removeObserver(repeatModeObserver!!)
            musicService.shuffleModeLiveData.removeObserver(shuffleModeObserver!!)
        }
    }

    fun setCurrentPlayerPosition(position: Int) {
        musicPlayerServiceRef?.get()?.let { musicService ->
            musicService.seekTo(position)
            if (isPlaying.value == true) {
                musicService.resume()
            }
        }
    }

    fun setIsFavorite() {
        // Implement favorite functionality if needed
    }

    fun playPreviousSong() {
        musicPlayerServiceRef?.get()?.playPrevious()
    }

    fun playNextSong() {
        musicPlayerServiceRef?.get()?.playNext()
    }

    fun setNextShuffleMode() {
        musicPlayerServiceRef?.get()?.setNextShuffleMode()
    }

    fun setNextRepeatMode() {
        musicPlayerServiceRef?.get()?.setNextRepeatMode()
    }

    fun getSongLyricText(): String {
        if (!isLyricLoaded) {
            _currentSong.value?.let { song ->
                lyricText = LyricUtility.loadLyricFromJson(song.title)
                isLyricLoaded = true
            }
        }
        return lyricText
    }
}