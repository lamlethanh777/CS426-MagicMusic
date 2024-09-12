package com.example.cs426_magicmusic.ui.view.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.Playlist
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
    private var albumItemAdapter: AlbumItemAdapter? = null
    private var artistItemAdapter: ArtistItemAdapter? = null
    private var playlistItemAdapter: PlaylistItemAdapter? = null
    private var displayOptionAdapter: DisplayOptionAdapter? = null
    private lateinit var lastFetchAction: (() -> Unit)
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var musicListRecyclerView: RecyclerView

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
        libraryViewModel.fetchAllAlbums()
        libraryViewModel.fetchAllArtists()
        libraryViewModel.fetchAllPlaylists()
        lastFetchAction = { libraryViewModel.fetchSongs() }

        Log.d(
            "Songs at LibraryFragment initialization",
            "${libraryViewModel.songs.value?.size ?: 0}"
        )
        Log.d(
            "Albums at LibraryFragment initialization",
            "${libraryViewModel.albums.value?.size ?: 0}"
        )
        Log.d(
            "Artists at LibraryFragment initialization",
            "${libraryViewModel.artists.value?.size ?: 0}"
        )
        Log.d(
            "Playlists at LibraryFragment initialization",
            "${libraryViewModel.playlists.value?.size ?: 0}"
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        setUpMusicListRecyclerView(view)
        setUpDisplayOptionRecyclerView(view)
        setUpToggleButton(view)
        subscribeToObservers()
        setClickListeners()
    }

    private fun setClickListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                LocalDBSynchronizer.synchronizeDatabase(requireContext())
                lastFetchAction.invoke()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun subscribeToObservers() {
        libraryViewModel.songs.observe(viewLifecycleOwner) {
            songItemAdapter?.itemList = it
        }
        libraryViewModel.albums.observe(viewLifecycleOwner) {
            albumItemAdapter?.itemList = it
        }
        libraryViewModel.artists.observe(viewLifecycleOwner) {
            artistItemAdapter?.itemList = it
        }
        libraryViewModel.playlists.observe(viewLifecycleOwner) {
            playlistItemAdapter?.itemList = it
        }

        Log.d(
            "Songs at LibraryFragment subscribeToObservers",
            "${libraryViewModel.songs.value?.size ?: 0}"
        )
        Log.d(
            "Albums at LibraryFragment subscribeToObservers",
            "${libraryViewModel.albums.value?.size ?: 0}"
        )
        Log.d(
            "Artists at LibraryFragment subscribeToObservers",
            "${libraryViewModel.artists.value?.size ?: 0}"
        )
        Log.d(
            "Playlists at LibraryFragment subscribeToObservers",
            "${libraryViewModel.playlists.value?.size ?: 0}"
        )
    }

    private fun setUpToggleButton(view: View) {
        view.findViewById<ToggleButton>(R.id.library_music_list_toggle)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    musicListRecyclerView.layoutManager = GridLayoutManager(
                        context, 2, GridLayoutManager.VERTICAL, false
                    )
                    songItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                    albumItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                    artistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                    playlistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                } else {
                    musicListRecyclerView.layoutManager = LinearLayoutManager(
                        context, LinearLayoutManager.VERTICAL, false
                    )
                    songItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                    albumItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                    artistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                    playlistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                }
            }
    }

    private fun setUpDisplayOptionRecyclerView(view: View) {
        val displayOptionList = listOf("Songs", "Albums", "Artists", "Playlists")
        displayOptionAdapter = DisplayOptionAdapter(displayOptionList, ::onClickDisplayOption)
        view.findViewById<RecyclerView>(R.id.library_display_option_recyclerview)
            .apply {
                adapter = displayOptionAdapter
                layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false
                )
            }
    }

    private fun onClickDisplayOption(displayOption: String) {
        Log.d("LibraryFragment", "onClickDisplayOption: $displayOption")
        when (displayOption) {
            "Songs" -> {
                lastFetchAction = { libraryViewModel.fetchSongs() }
                musicListRecyclerView.adapter = songItemAdapter
            }
            "Albums" -> {
                lastFetchAction = { libraryViewModel.fetchAllAlbums() }
                musicListRecyclerView.adapter = albumItemAdapter
            }
            "Artists" -> {
                lastFetchAction = { libraryViewModel.fetchAllArtists() }
                musicListRecyclerView.adapter = artistItemAdapter
            }
            "Playlists" -> {
                lastFetchAction = { libraryViewModel.fetchAllPlaylists() }
                musicListRecyclerView.adapter = playlistItemAdapter
            }
        }
        lastFetchAction.invoke()
    }

    private fun setUpMusicListRecyclerView(view: View) {
        songItemAdapter = SongItemAdapter(::onClickSongItem)
        albumItemAdapter = AlbumItemAdapter(::onClickAlbumItem)
        artistItemAdapter = ArtistItemAdapter(::onClickArtistItem)
        playlistItemAdapter = PlaylistItemAdapter(::onClickPlaylistItem)
        musicListRecyclerView = view.findViewById(R.id.library_music_list_recyclerview)
        musicListRecyclerView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
    }

    private fun onClickSongItem(song: Song?) {
        Log.d("LibraryFragment", "onClickSongItem: ${song?.title}")
        var intent: Intent = Intent(context, SongPlayerActivity::class.java)
        intent.putExtra(INTENT_KEY_NEW_SONG, song)
        intent.action = ACTION_PLAY_NEW_SONG
        startActivity(intent)
    }

    private fun onClickAlbumItem(album: Album?) {
        Log.d("LibraryFragment", "onClickAlbumItem: ${album?.albumName}")
        lastFetchAction = { libraryViewModel.fetchSongsInAlbum(album!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
    }

    private fun onClickArtistItem(artist: Artist?) {
        Log.d("LibraryFragment", "onClickArtistItem: ${artist?.artistName}")
        lastFetchAction = { libraryViewModel.fetchSongsOfArtist(artist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
    }

    private fun onClickPlaylistItem(playlist: Playlist?) {
        Log.d("LibraryFragment", "onClickPlaylistItem: ${playlist?.playlistName}")
        lastFetchAction = { libraryViewModel.fetchSongsInPlaylist(playlist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
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
