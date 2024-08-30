package com.example.cs426_magicmusic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun initial() {
        replaceFragment(LibraryFragment.newInstance())
        setCurrentMusic(MusicItem("music title", listOf("author 1", "author 2"), R.drawable.home_black_25_24, 0, 0))
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_fragment, fragment).commit()
    }

    private fun setCurrentMusic(musicItem: MusicItem) {
        currentMusicItem = musicItem
        currentMusicIcon.setBackgroundResource(musicItem.icon)
        currentMusicTitle.text = musicItem.title
        currentMusicAuthor.text = musicItem.author.joinToString(", ")
//        TODO("current music play pause state is not defined")
        // currentMusicPlayPause
    }

    public fun getCurrentMusic() : MusicItem {
        return currentMusicItem
    }
}