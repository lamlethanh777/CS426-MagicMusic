package com.example.cs426_magicmusic.ui.view.songplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.abdelhakim.prosoundeq.ProSoundEQ
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_NEW_SONG
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_NEW_SONG_LIST
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_SONG_INDEX
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_ALL
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_NONE
import com.example.cs426_magicmusic.others.Constants.PLAYER_REPEAT_MODE_ONE
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_IMAGE
import com.example.cs426_magicmusic.service.musicplayer.MusicPlayerService
import com.example.cs426_magicmusic.ui.viewmodel.SongPlayerViewModel
import com.example.cs426_magicmusic.utils.ImageUtility
import com.example.cs426_magicmusic.utils.IntentUtility.parcelableArrayList
import com.example.cs426_magicmusic.utils.PaletteUtility
import com.example.cs426_magicmusic.utils.TimeFormatUtility.formatTimestampToMMSS
import kotlinx.coroutines.launch

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
    private lateinit var repeatButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var alarmOffButton: ImageButton
    private lateinit var equalizerButton: ImageButton
    private lateinit var lyricButton: ImageButton
    private lateinit var innerLayout: CardView
    private lateinit var outerLayout: ScrollView

    private lateinit var musicPlayerService: MusicPlayerService

    private var shouldUpdateSeekBar = true
    private var isServiceBound = false
    private var songList = mutableListOf<Song>()
    private var songIndex = 0
    private var incomingIntentAction: String? = null

    private val songPlayerViewModel = SongPlayerViewModel()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            songPlayerViewModel.setMusicService(musicPlayerService)
            isServiceBound = true

            Log.d("SongPlayerActivity", "onServiceConnected")

            when (incomingIntentAction) {
                ACTION_PLAY_NEW_SONG -> {
                    Log.d("SongPlayerActivity", "Playing new song at index $songIndex")
                    musicPlayerService.setPlaylist(songList, songIndex)
                    musicPlayerService.playCurrent()
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        incomingIntentAction = intent.action
        intent.extras?.let {
            val songs = it.parcelableArrayList<Song>(INTENT_KEY_NEW_SONG_LIST)
            if (songs != null) {
                songList.clear()
                songList.addAll(songs)
                Log.d("SongPlayerActivity", "New song list: ${songList.size}")
            }

            songIndex = it.getInt(INTENT_KEY_SONG_INDEX, 0)
            Log.d("SongPlayerActivity", "New song index: $songIndex")
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
        repeatButton = findViewById(R.id.play_music_repeat)
        shuffleButton = findViewById(R.id.play_music_shuffle)
        alarmOffButton = findViewById(R.id.play_music_alarm_off)
        equalizerButton = findViewById(R.id.play_music_equalizer)
        lyricButton = findViewById(R.id.play_music_lyric)
        innerLayout = findViewById(R.id.play_inner_layout)
        outerLayout = findViewById(R.id.play_layout)

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
        setRepeatButtonListener()
        setShuffleButtonListener()
        setAlarmOffButtonListener()
    }

    private fun setAlarmOffButtonListener() {

    }

    private fun setShuffleButtonListener() {
        shuffleButton.setOnClickListener {
            songPlayerViewModel.setNextShuffleMode()
        }
    }

    private fun setRepeatButtonListener() {
        repeatButton.setOnClickListener {
            songPlayerViewModel.setNextRepeatMode()
        }
        setEqualizerListener()
        setShowLyricListener()
    }

    private fun setShowLyricListener() {
        lyricButton.setOnClickListener {
            val lyrics = songPlayerViewModel.getSongLyricText()
            val bottomSheetFragment = LyricBottomSheetFragment.newInstance(lyrics)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun setEqualizerListener() {
        equalizerButton.setOnClickListener {
            startActivity(Intent(this, ProSoundEQ::class.java))
        }
    }

    private fun setSkipPreviousButtonListener() {
        skipPreviousButton.setOnClickListener {
            songPlayerViewModel.playPreviousSong()
        }
    }

    private fun setSkipNextButtonListener() {
        skipNextButton.setOnClickListener {
            songPlayerViewModel.playNextSong()
        }
    }

    private fun setIsFavoriteButtonListener() {
        favoriteButton.setOnClickListener {
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

    private fun setRetractButtonListener() {
        retractButton.setOnClickListener {
            finish()
        }
    }

    private fun subscribeToObservers() {
        subscribeToCurrentSongLiveData()
        subscribeToCurrentSongPositionLiveData()
        subscribeToIsPlayingLiveData()
        subscribeToIsFavoriteLiveData()
        subscribeToRepeatModeLiveData()
        subscribeToShuffleModeLiveData()
        subscribeToAlarmOffLiveData()
    }

    private fun subscribeToAlarmOffLiveData() {

    }

    private fun subscribeToShuffleModeLiveData() {
        songPlayerViewModel.shuffleMode.observe(this@SongPlayerActivity) { shuffleMode ->
            when (shuffleMode) {
                false -> shuffleButton.setImageResource(R.drawable.ic_no_shuffle_30)
                true -> shuffleButton.setImageResource(R.drawable.ic_shuffle_30)
            }
        }
    }

    private fun subscribeToRepeatModeLiveData() {
        songPlayerViewModel.repeatMode.observe(this@SongPlayerActivity) { repeatMode ->
            when (repeatMode) {
                PLAYER_REPEAT_MODE_NONE -> repeatButton.setImageResource(R.drawable.ic_no_repeat_30)
                PLAYER_REPEAT_MODE_ONE -> repeatButton.setImageResource(R.drawable.ic_repeat_one_30)
                PLAYER_REPEAT_MODE_ALL -> repeatButton.setImageResource(R.drawable.ic_repeat_all_30)
            }
        }
    }

    private fun subscribeToIsFavoriteLiveData() {

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
            Log.d("SongPlayerActivity", "Current song: ${song.title}")

            // Try to load from Url first
            val urlTmp = ImageUtility.loadImageUrlFromJson(song.title)
            var tmpBitmap: Bitmap? = null

            // There is Url
            if (urlTmp != STRING_UNKNOWN_IMAGE) {
                lifecycleScope.launch {
                    tmpBitmap = ImageUtility.loadBitmapFromUrl(this@SongPlayerActivity, song.title)
                    if (tmpBitmap != null) {
                        // Can load Url -> Bitmap
                        PaletteUtility.applyPaletteFromImage(
                            this@SongPlayerActivity,
                            tmpBitmap!!,
                            innerLayout,
                            outerLayout
                        )
                        Log.d("SongPlayerActivity", "Palette applied")
                    }
                }
            }

            // Cannot load Url -> load Uri or default placeholder
            if (tmpBitmap == null) {
                tmpBitmap = ImageUtility.loadBitmapFromUri(this, song.uri)
                PaletteUtility.applyPaletteFromImage(this, tmpBitmap, innerLayout, outerLayout)
            }

            ImageUtility.loadImage(this, song.title, song.uri, imageView)

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