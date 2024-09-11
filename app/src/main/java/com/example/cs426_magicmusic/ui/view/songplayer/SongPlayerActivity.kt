package com.example.cs426_magicmusic.ui.view.songplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_NEW_SONG
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_NEW_SONG
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_ARTIST
import com.example.cs426_magicmusic.service.musicplayer.MusicPlayerService
import com.example.cs426_magicmusic.ui.viewmodel.SongPlayerViewModel
import com.example.cs426_magicmusic.utils.ImageUtility
import com.example.cs426_magicmusic.utils.IntentUtility.parcelable
import com.example.cs426_magicmusic.utils.TimeFormatUtility.formatTimestampToMMSS

class SongPlayerActivity : AppCompatActivity() {
    private lateinit var textFrom: TextView
    private lateinit var textTo: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var favoriteButton: ImageButton
    private lateinit var songTitle: TextView
    private lateinit var artistNames: TextView
    private lateinit var imageView: ImageView
    private lateinit var retractButton: ImageButton
    private lateinit var skipNextButton: ImageButton
    private lateinit var skipPreviousButton: ImageButton

    private lateinit var musicPlayerService: MusicPlayerService

    private var shouldUpdateSeekBar = true
    private var isServiceBound = false
    private var songList = mutableListOf<Song>()
    private var incomingIntentAction: String? = null

    private val songPlayerViewModel = SongPlayerViewModel()

    /**
     * Service connection: bind the music service to the activity
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            songPlayerViewModel.setMusicService(musicPlayerService)
            isServiceBound = true

            when (incomingIntentAction) {
                ACTION_PLAY_NEW_SONG -> {
                    musicPlayerService.setPlaylist(songList)
                    musicPlayerService.playNextSong()
                }

                else -> {
                    Log.d("SongPlayerActivity", "No action")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("SongPlayerActivity", "onServiceDisconnected")
            isServiceBound = false
        }
    }

    /**
     * Function to handle new incoming intent (e.g., play new song)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incomingIntentAction = intent.action
        intent.extras?.apply {
            when {
                containsKey(INTENT_KEY_NEW_SONG) -> {
                    val song: Song? = intent.parcelable(INTENT_KEY_NEW_SONG)
                    if (song != null) {
                        songList.add(song)
                    }
                }

                else -> {
                    Log.d("SongPlayerActivity", "No new song")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play)

        onNewIntent(intent)
        initializeViews()
        subscribeToObservers()
        setClickListeners()
    }

    private fun initializeViews() {
        favoriteButton = findViewById(R.id.play_music_favorite)
        seekBar = findViewById(R.id.play_music_seekbar)
        textFrom = findViewById(R.id.play_music_from)
        textTo = findViewById(R.id.play_music_to)
        playPauseButton = findViewById(R.id.play_music_play_pause)
        songTitle = findViewById(R.id.play_music_title)
        artistNames = findViewById(R.id.play_music_author)
        imageView = findViewById(R.id.play_music_wall)
        retractButton = findViewById(R.id.play_retract_button)
        skipNextButton = findViewById(R.id.play_music_next)
        skipPreviousButton = findViewById(R.id.play_music_previous)

        songTitle.isSelected = true
        artistNames.isSelected = true
    }

    private fun setClickListeners() {
        setSeekBarListener()
        setPlayPauseButtonListener()
        setRetractButtonListener()
        setIsFavoriteButtonListener()
        setSkipNextButtonListener()
        setSkipPreviousButtonListener()
    }

    private fun setSkipPreviousButtonListener() {
        skipPreviousButton.setOnClickListener() {
            songPlayerViewModel.skipPreviousSong()
        }
    }

    private fun setSkipNextButtonListener() {
        skipNextButton.setOnClickListener() {
            songPlayerViewModel.skipNextSong()
        }
    }

    private fun setIsFavoriteButtonListener() {
        favoriteButton.setOnClickListener() {
            songPlayerViewModel.setIsFavorite()
        }
    }

    private fun setSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    textFrom.text = formatTimestampToMMSS(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekBar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    songPlayerViewModel.setCurrentPlayerPosition(it.progress)
                    shouldUpdateSeekBar = true
                }
            }
        })
    }

    private fun setPlayPauseButtonListener() {
        playPauseButton.setOnClickListener {
            songPlayerViewModel.setIsPlaying()
        }
    }

    /**
     * Function to set the retract button listener (return from SongPlayerActivity -> MainActivity)
     */
    private fun setRetractButtonListener() {
        retractButton.setOnClickListener {
            finish()
        }
    }

    private fun subscribeToObservers() {
        subscribeToCurrentSongLiveData()
        subscribeToCurrentSongPositionLiveData()
        subscribeToIsPlayingLiveData()
    }

    private fun subscribeToIsPlayingLiveData() {
        songPlayerViewModel.isPlaying.observe(this@SongPlayerActivity) { isPlaying ->
            when {
                isPlaying -> playPauseButton.setImageResource(R.drawable.ic_pause_circle_90)
                else -> playPauseButton.setImageResource(R.drawable.ic_play_circle_90)
            }
        }
    }

    private fun subscribeToCurrentSongPositionLiveData() {
        songPlayerViewModel.currentSongPosition.observe(this@SongPlayerActivity) { currentPosition ->
            if (shouldUpdateSeekBar) {
                seekBar.progress = currentPosition
                textFrom.text = formatTimestampToMMSS(currentPosition)
            }
        }
    }

    private fun subscribeToCurrentSongLiveData() {
        songPlayerViewModel.currentSong.observe(this@SongPlayerActivity) { song ->
            ImageUtility.loadImage(this, song.uri, imageView)

            songTitle.text = song.title
            artistNames.text = song.artistNames

            // TODO: DANGEROUS. FIX THIS
            seekBar.max = song.duration.toInt()
            textTo.text = formatTimestampToMMSS(song.duration.toInt())

            when {
                song.isFavorite -> favoriteButton.setImageResource(R.drawable.ic_liked_30)
                else -> favoriteButton.setImageResource(R.drawable.ic_like_30)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicPlayerService::class.java)
        // Start the music service
        startService(intent)

        // Bind to the service
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        Log.d("SongPlayerActivity", "Music service started")
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}