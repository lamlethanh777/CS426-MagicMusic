// MainViewModel.kt
package com.example.cs426_magicmusic.ui.viewmodel

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.service.music_player.MusicPlayerService
import com.example.cs426_magicmusic.ui.view.main.library.LibraryFragment
import java.lang.ref.WeakReference

class MainViewModel : ViewModel() {
    private var currentFragment: Fragment = LibraryFragment.newInstance()

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    // WeakReference to avoid memory leaks
    private var musicPlayerServiceRef: WeakReference<MusicPlayerService>? = null

    fun setCurrentFragment(fragment: Fragment) {
        currentFragment = fragment
    }

    fun getCurrentFragment(): Fragment {
        return currentFragment
    }

    fun setIsPlaying() {
        _isPlaying.value = !(isPlaying.value ?: false)
        if (isPlaying.value == true) {
            musicPlayerServiceRef?.get()?.resume()
        } else {
            musicPlayerServiceRef?.get()?.pause()
        }
    }

    // Store the observer so that we can remove it later
    private var currentSongObserver: Observer<Song?>? = null
    private var isPlayingObserver: Observer<Boolean>? = null

    // Function to set the MusicPlayerService and observe its LiveData
    fun setMusicService(musicPlayerService: MusicPlayerService) {
        musicPlayerServiceRef = WeakReference(musicPlayerService)

        currentSongObserver = Observer { song ->
            song?.let {
                Log.d("Next song", it.toString())
                _currentSong.value = it
            } ?: run {
                Log.e("MainViewModel", "Current song is null")
            }
        }
        isPlayingObserver = Observer {
            _isPlaying.value = it
        }

        Log.d("MainViewModel", "Setting music service")

        currentSongObserver?.let {
            musicPlayerService.currentSongLiveData.observeForever(it)
        }
        isPlayingObserver?.let {
            musicPlayerService.isPlayingLiveData.observeForever(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerServiceRef?.get()?.let { musicService ->
            currentSongObserver?.let { musicService.currentSongLiveData.removeObserver(it) }
            isPlayingObserver?.let { musicService.isPlayingLiveData.removeObserver(it) }
        }
    }
}