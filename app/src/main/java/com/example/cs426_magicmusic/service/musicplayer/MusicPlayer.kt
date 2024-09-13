package com.example.cs426_magicmusic.service.musicplayer

import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants.DEFAULT_STARTING_AUDIO_POSITION
import com.example.cs426_magicmusic.others.Constants.NUMBER_OF_REPEAT_MODE
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_ALL
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_NONE
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_ONE
import com.example.cs426_magicmusic.others.Constants.PLAYER_SHUFFLE_MODE_OFF
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayer(private val service: LifecycleService) {
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(service).build()
    private val _currentSongLiveData = MutableLiveData<Song>()
    val currentSongLiveData: LiveData<Song> = _currentSongLiveData
    private val _isPlayingLiveData = MutableLiveData<Boolean>()
    val isPlayingLiveData: LiveData<Boolean> = _isPlayingLiveData
    private val _currentSongPositionLiveData = MutableLiveData<Int>()
    val currentSongPositionLiveData: LiveData<Int> = _currentSongPositionLiveData
    private val _repeatModeLiveData = MutableLiveData(PLAYER_REPEAT_MODE_NONE)
    val repeatModeLiveData: LiveData<Int> = _repeatModeLiveData
    private val _shuffleModeLiveData = MutableLiveData(PLAYER_SHUFFLE_MODE_OFF)
    val shuffleModeLiveData: LiveData<Boolean> = _shuffleModeLiveData
    private var currentJob = service.lifecycleScope.launch {}
    private var playlist: List<Song> = listOf()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {

                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    val currentSong = playlist[exoPlayer.currentMediaItemIndex]
                    _currentSongLiveData.postValue(currentSong)
                    Log.d("MusicPlayer", "Media item transitioned to: ${currentSong.title}")
                }
            }
        })

        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        exoPlayer.shuffleModeEnabled = false
        _currentSongPositionLiveData.value = 0
        _isPlayingLiveData.value = false
        Log.d("MusicPlayer", "MusicPlayer initialized")
    }

    fun setPlaylist(songs: List<Song>, songIndex: Int = 0) {
        Log.d("MusicPlayer", "Number of songs: ${songs.size}")
        playlist = songs
        val mediaItems = songs.map { MediaItem.fromUri(it.path) }
        exoPlayer.setMediaItems(mediaItems, songIndex, 0L)
        Log.d("MusicPlayer", "Playlist set")
    }

    private fun play() {
        exoPlayer.prepare()
        exoPlayer.play()
        startUpdatingPosition()

        // Update LiveData
        _isPlayingLiveData.postValue(true)
        _currentSongLiveData.postValue(playlist[exoPlayer.currentMediaItemIndex])

        Log.d("MusicPlayer", "Playing ${playlist[exoPlayer.currentMediaItemIndex].title}")
    }

    private fun startUpdatingPosition() {
        currentJob.cancel()
        currentJob = service.lifecycleScope.launch {
            while (isActive) {
                _currentSongPositionLiveData.postValue(exoPlayer.currentPosition.toInt())
                delay(100L)
            }
        }
    }

    fun playCurrent() {
        play()
    }

    fun playNext() {
        exoPlayer.seekToNext()
        play()
    }

    fun playPrevious() {
        exoPlayer.seekToPrevious()
        play()
    }

    fun pause() {
        exoPlayer.pause()
        _isPlayingLiveData.postValue(false)
    }

    fun resume() {
        exoPlayer.play()
        _isPlayingLiveData.postValue(true)
    }

    fun setNextRepeatMode() {
        val currentValue = _repeatModeLiveData.value ?: PLAYER_REPEAT_MODE_NONE
        val nextValue = (currentValue + 1) % NUMBER_OF_REPEAT_MODE
        _repeatModeLiveData.value = nextValue
        exoPlayer.repeatMode = when (nextValue) {
            PLAYER_REPEAT_MODE_NONE -> Player.REPEAT_MODE_OFF
            PLAYER_REPEAT_MODE_ONE -> Player.REPEAT_MODE_ONE
            PLAYER_REPEAT_MODE_ALL -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun setNextShuffleMode() {
        exoPlayer.shuffleModeEnabled = exoPlayer.shuffleModeEnabled != true
        _shuffleModeLiveData.value = exoPlayer.shuffleModeEnabled
    }

    fun getCurrentSong(): Song? {
        return _currentSongLiveData.value
    }

    fun seekTo(position: Int) {
        val duration = exoPlayer.duration.toInt()
        val safePosition = when {
            position < 0 -> 0
            position > duration -> duration
            else -> position
        }
        exoPlayer.seekTo(safePosition.toLong())
    }

    fun release() {
        exoPlayer.release()
    }
}