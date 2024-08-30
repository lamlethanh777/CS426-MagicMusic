package com.example.cs426_magicmusic

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.play_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val musicItem: MusicItem = intent.getSerializableExtra("MUSIC_ITEM") as MusicItem
        findViewById<View>(R.id.play_music_icon).setBackgroundResource(musicItem.icon)
        findViewById<TextView>(R.id.play_music_title).text = musicItem.title
        findViewById<TextView>(R.id.play_music_author).text = musicItem.author.joinToString(", ")

        val favoriteButton: ImageButton = findViewById(R.id.play_music_favorite)
    }
}