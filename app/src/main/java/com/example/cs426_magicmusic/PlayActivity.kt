package com.example.cs426_magicmusic

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlayActivity : AppCompatActivity() {

    private var musicIsPlaying = false
    private var duration = 0

    private lateinit var textFrom: TextView
    private lateinit var textTo:   TextView
    private lateinit var playPauseButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.play_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        duration = PlayAudioManager.getAudioLong()

        val musicItem: MusicItem = intent.getSerializableExtra("MUSIC_ITEM") as MusicItem
        findViewById<View>(R.id.play_music_wall).setBackgroundResource(musicItem.icon)
        findViewById<View>(R.id.play_music_icon).setBackgroundResource(musicItem.icon)
        findViewById<TextView>(R.id.play_music_title).text = musicItem.title
        findViewById<TextView>(R.id.play_music_author).text = musicItem.author.joinToString(", ")

        val favoriteButton: ImageButton = findViewById(R.id.play_music_favorite)

        val progressBar: SeekBar = findViewById(R.id.play_music_seekbar)
        progressBar.max = duration
        textFrom = findViewById(R.id.play_music_from)
        textTo   = findViewById(R.id.play_music_to)

        val handler = Handler(Looper.getMainLooper())
        val updateProgressBar: Runnable = object : Runnable {
            @SuppressLint("SetTextI18n")
            override fun run() {
                // update progressBar and delay 1 second
                val timestamp = PlayAudioManager.getCurrentTimestamp()
                progressBar.progress = timestamp
                textFrom.text = formatTimestampToMMSS(timestamp)
                textTo.text = "-" + formatTimestampToMMSS(duration - timestamp)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateProgressBar)

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    PlayAudioManager.setCurrentTimestamp(progress) // Cho phép người dùng thay đổi vị trí phát nhạc
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Tạm dừng cập nhật khi người dùng bắt đầu thay đổi
                handler.removeCallbacks(updateProgressBar)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Tiếp tục cập nhật khi người dùng dừng thay đổi
                handler.post(updateProgressBar)
            }
        })

        playPauseButton = findViewById(R.id.play_music_play_pause)
        playPauseButton.setOnClickListener {
            setPlayingState(!musicIsPlaying)
        }

        setPlayingState(PlayAudioManager.isPlaying())
    }

    private fun setPlayingState(state: Boolean) {
        musicIsPlaying = state
        if (state) {
            PlayAudioManager.play()
            playPauseButton.setBackgroundResource(R.drawable.played_button)
        }
        else {
            PlayAudioManager.pause()
            playPauseButton.setBackgroundResource(R.drawable.paused_button)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTimestampToMMSS(timestamp: Int): String {
        val minutes = timestamp / 1000 / 60
        val seconds = (timestamp / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}