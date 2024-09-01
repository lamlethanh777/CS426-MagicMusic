package com.example.cs426_magicmusic

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(),
    LibraryFragment.ButtonListeners {

    override fun LibraryFragment_onMusicItemButton(musicItem: MusicItem) {
        setCurrentMusic(musicItem)
        currentMusicLayout.callOnClick()
    }

    private lateinit var currentMusicItem: MusicItem
    private lateinit var currentMusicIcon: ImageView
    private lateinit var currentMusicTitle: TextView
    private lateinit var currentMusicAuthor: TextView
    private lateinit var currentMusicPlayPause: ImageButton
    private lateinit var currentMusicLayout: ConstraintLayout

    private var musicIsPlaying: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MusicAdapter.init(this)
        PlayAudioManager.init(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentMusicLayout = findViewById(R.id.current_music_layout)
        currentMusicLayout.setOnClickListener {
            val intent = Intent(this, PlayActivity::class.java)
            intent.putExtra("MUSIC_ITEM", getCurrentMusic())
            startActivity(intent)
        }

        currentMusicIcon = findViewById(R.id.current_music_icon)
        currentMusicTitle = findViewById(R.id.current_music_title)
        currentMusicAuthor = findViewById(R.id.current_music_author)
        currentMusicPlayPause = findViewById(R.id.current_music_play_pause)
        currentMusicPlayPause.setOnClickListener {
            setPlayingState(!musicIsPlaying)
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_library -> {
                    replaceFragment(LibraryFragment.newInstance())
                }
                R.id.navigation_search -> {
                    replaceFragment(SearchFragment.newInstance())
                }
                R.id.navigation_generate_audio -> {
                    replaceFragment(GenerateAudioFragment.newInstance())
                }
            }
            true
        }

        initial()
    }

    override fun onStart() {
        super.onStart()
        setPlayingState(PlayAudioManager.isPlaying())
    }

    private fun initial() {
        replaceFragment(LibraryFragment.newInstance())
        setCurrentMusic(MusicRepository.getMusicList("all")[0], true)
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_fragment, fragment).commit()
    }

    private fun setCurrentMusic(musicItem: MusicItem, isInitial: Boolean = false) {
        currentMusicItem = musicItem
        currentMusicIcon.setBackgroundResource(musicItem.icon)
        currentMusicTitle.text = musicItem.title
        currentMusicAuthor.text = musicItem.author.joinToString(", ")
        PlayAudioManager.setResource(MusicRepository.getAudio(musicItem.audioId))
        setPlayingState(!isInitial)
    }

    fun getCurrentMusic() : MusicItem {
        return currentMusicItem
    }

    private fun setPlayingState(state: Boolean) {
        musicIsPlaying = state
        if (state) {
            PlayAudioManager.play()
            currentMusicPlayPause.setBackgroundResource(R.drawable.played_button)
        }
        else {
            PlayAudioManager.pause()
            currentMusicPlayPause.setBackgroundResource(R.drawable.paused_button)
        }
    }
}