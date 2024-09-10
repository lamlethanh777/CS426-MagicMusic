package com.example.cs426_magicmusic.ui.view.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.repository.AlbumRepository
import com.example.cs426_magicmusic.data.repository.ArtistRepository
import com.example.cs426_magicmusic.data.repository.PlaylistRepository
import com.example.cs426_magicmusic.data.repository.SongRepository
import com.example.cs426_magicmusic.data.source.db.AppDatabase
import com.example.cs426_magicmusic.data.source.db.synchronize.LocalDBSynchronizer
import com.example.cs426_magicmusic.others.Constants.ACTION_PLAY_NEW_SONG
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_NEW_SONG
import com.example.cs426_magicmusic.ui.view.songplayer.SongPlayerActivity
import com.example.cs426_magicmusic.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private var songItemAdapter: SongItemAdapter? = null
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private fun initViewModel() {
        val appDatabase = AppDatabase.getDatabase(requireContext())
        var songRepository = SongRepository(appDatabase)
        var albumRepository = AlbumRepository(appDatabase)
        var artistRepository = ArtistRepository(appDatabase)
        var playlistRepository = PlaylistRepository(appDatabase)

        LocalDBSynchronizer.setupRepositories(albumRepository, artistRepository, songRepository)
        libraryViewModel = LibraryViewModel(
            songRepository, albumRepository, artistRepository, playlistRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_library, container, false)
        libraryViewModel.fetchSongs()

        Log.d("Songs at LibraryFragment initialization",
            "${libraryViewModel.songs.value?.size?:0}"
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        setUpRecyclerView(view)
        subscribeToObservers()
        setClickListeners()
    }

    private fun setClickListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                LocalDBSynchronizer.synchronizeDatabase(requireContext())
                libraryViewModel.fetchSongs()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun subscribeToObservers() {
        libraryViewModel.songs.observe(viewLifecycleOwner) {
            songItemAdapter?.songList = it
        }
        Log.d("Songs at LibraryFragment subscribeToObservers",
            "${libraryViewModel.songs.value?.size?:0}"
        )
    }

    private fun setUpRecyclerView(view: View) {
        songItemAdapter = SongItemAdapter(::onClickSongItem)
        view.findViewById<RecyclerView>(R.id.library_music_list_recyclerview)
            .apply {
                adapter = songItemAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false
                )
            }
    }

    private fun onClickSongItem(song: Song?) {
        Log.d("LibraryFragment", "onClickSongItem: ${song?.title}")
        var intent: Intent = Intent(context, SongPlayerActivity::class.java)
        intent.putExtra(INTENT_KEY_NEW_SONG, song)
        intent.action = ACTION_PLAY_NEW_SONG
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LibraryFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }
}
