package com.example.cs426_magicmusic.ui.view.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.PlaylistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import com.example.cs426_magicmusic.data.source.db.AppDatabase
import com.example.cs426_magicmusic.data.source.db.synchronize.LocalDBSynchronizer
import com.example.cs426_magicmusic.others.Constants.ACTION_RETURN_TO_SONG_PLAYER_ACTIVITY
import com.example.cs426_magicmusic.service.music_player.MusicPlayerService
import com.example.cs426_magicmusic.ui.view.main.about.AboutFragment
import com.example.cs426_magicmusic.ui.view.main.generate_audio.GenerateAudioFragment
import com.example.cs426_magicmusic.ui.view.main.library.LibraryFragment
import com.example.cs426_magicmusic.ui.view.main.search.SearchFragment
import com.example.cs426_magicmusic.ui.view.song_player.SongPlayerActivity
import com.example.cs426_magicmusic.ui.viewmodel.MainViewModel
import com.example.cs426_magicmusic.utils.ImageUtility
import com.example.cs426_magicmusic.utils.PermissionUtility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var currentSongImage: ImageView
    private lateinit var currentSongTitle: TextView
    private lateinit var currentArtistNames: TextView
    private lateinit var currentSongPlayPause: ImageButton
    private lateinit var currentSongLayout: ConstraintLayout

    private lateinit var musicPlayerService: MusicPlayerService

    private val mainViewModel: MainViewModel by viewModels()
    private var isServiceBound = false

    /**
     * Service connection: bind the music service to the activity
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.LocalBinder
            musicPlayerService = binder.getService()
            isServiceBound = true

            Log.d("MainActivity", "Service connected")

            mainViewModel.setMusicService(musicPlayerService)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CS426MagicMusic)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        PermissionUtility.requestPermissions(this)
        if (PermissionUtility.hasEnoughPermission(this)) {
            initializeDatabase()
        }

        initializeViews()
        subscribeToObservers()
        setClickListeners()
        replaceFragment(mainViewModel.getCurrentFragment())
    }

    private fun initializeViews() {
        currentSongImage = findViewById(R.id.current_song_image)
        currentSongTitle = findViewById(R.id.current_song_title)
        currentArtistNames = findViewById(R.id.current_song_author)
        currentSongPlayPause = findViewById(R.id.current_song_play_pause)
        currentSongLayout = findViewById(R.id.current_song_layout)

        currentSongTitle.isSelected = true
        currentArtistNames.isSelected = true
    }

    private fun initializeDatabase() {
        val appDatabase = AppDatabase.getDatabase(this)
        val songRepository = SongRepository(appDatabase)
        val albumRepository = AlbumRepository(appDatabase)
        val artistRepository = ArtistRepository(appDatabase)
        val playlistRepository = PlaylistRepository(appDatabase)
        LocalDBSynchronizer.setupRepositories(
            albumRepository, artistRepository, songRepository, playlistRepository
        )
        lifecycleScope.launch {
            LocalDBSynchronizer.synchronizeDatabase(this@MainActivity)
        }
    }

    private fun setClickListeners() {
        setCurrentSongLayoutListener()
        setCurrentSongPlayPauseListener()
        setBottomNavigationListener()
    }

    private fun setCurrentSongPlayPauseListener() {
        currentSongPlayPause.setOnClickListener {
            mainViewModel.setIsPlaying()
        }
    }

    private fun setCurrentSongLayoutListener() {
        // Default: hide the current song layout -> show when there is a song playing
        currentSongLayout.visibility = View.GONE

        currentSongLayout.setOnClickListener {
            val intent = Intent(this, SongPlayerActivity::class.java)
            intent.action = ACTION_RETURN_TO_SONG_PLAYER_ACTIVITY
            Log.d("MainActivity", "Return SongPlayerActivity")
            startActivity(intent)
        }
    }

    private fun setBottomNavigationListener() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnItemSelectedListener { item ->
            var fragment: Fragment = LibraryFragment.newInstance()
            when (item.itemId) {
                R.id.navigation_library -> {
                    fragment = LibraryFragment.newInstance()
                }

                R.id.navigation_search -> {
                    fragment = SearchFragment.newInstance()
                }

                R.id.navigation_generate_audio -> {
                    fragment = GenerateAudioFragment.newInstance()
                }

                R.id.navigation_about -> {
                    fragment = AboutFragment.newInstance()
                }
            }
            mainViewModel.setCurrentFragment(fragment)
            replaceFragment(fragment)
            true
        }
    }

    private fun subscribeToObservers() {
        subscribeToCurrentSongLiveData()
        subscribeToIsPlayingLiveData()
    }

    private fun subscribeToIsPlayingLiveData() {
        mainViewModel.isPlaying.observe(this@MainActivity) { isPlaying ->
            when {
                isPlaying -> currentSongPlayPause.setImageResource(R.drawable.ic_pause_circle_40)
                else -> currentSongPlayPause.setImageResource(R.drawable.ic_play_circle_40)
            }
        }
    }

    private fun subscribeToCurrentSongLiveData() {
        mainViewModel.currentSong.observe(this@MainActivity) { song ->
            if (song == null) {
                currentSongLayout.visibility = View.GONE
                return@observe
            }

            currentSongLayout.visibility = View.VISIBLE
            ImageUtility.loadImage(this, song.title, song.uri, currentSongImage)
            currentSongTitle.text = song.title
            currentArtistNames.text = song.artistNames
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicPlayerService::class.java)

        // Start the music service
        startService(intent)

        // Bind to the service
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d("MainActivity", "Music service started")
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "onDestroy called")
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_fragment, fragment).commit()
        Log.d(
            "MainActivity",
            "current fragment: ${mainViewModel.getCurrentFragment()::class.java.simpleName}"
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            PermissionUtility.requestPermissions(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (PermissionUtility.hasEnoughPermission(this)) {
            Toast.makeText(
                this,
                "Permission successfully granted!",
                Toast.LENGTH_SHORT
            ).show()
            initializeDatabase()
        }
    }
}