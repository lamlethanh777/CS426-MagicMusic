package com.example.cs426_magicmusic.ui.view.main.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
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
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_NEW_SONG_LIST
import com.example.cs426_magicmusic.others.Constants.INTENT_KEY_SONG_INDEX
import com.example.cs426_magicmusic.ui.view.songplayer.SongPlayerActivity
import com.example.cs426_magicmusic.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private var songItemAdapter: SongItemAdapter? = null
    private var albumItemAdapter: AlbumItemAdapter? = null
    private var artistItemAdapter: ArtistItemAdapter? = null
    private var playlistItemAdapter: PlaylistItemAdapter? = null
    private var displayOptionAdapter: DisplayOptionAdapter? = null
    private var listenerManager = ListenerManager()
    private var currentSongList = mutableListOf<Song>()
    private lateinit var lastFetchAction: (() -> Unit)
    private lateinit var libraryViewModel: LibraryViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var musicListRecyclerView: RecyclerView
    private lateinit var popupButton: ImageButton
    private lateinit var popupSongs: () -> Unit
    private lateinit var popupSongsInAlbum: (Album) -> Unit
    private lateinit var popupSongsOfArtist: (Artist) -> Unit
    private lateinit var popupSongsInPlaylist: (Playlist) -> Unit
    private lateinit var popupAlbums: () -> Unit
    private lateinit var popupArtists: () -> Unit
    private lateinit var popupPlaylists: () -> Unit

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

        setUpPopupMenu(view)
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
                popupButton.setOnClickListener { popupSongs.invoke() }
            }

            "Albums" -> {
                lastFetchAction = { libraryViewModel.fetchAllAlbums() }
                musicListRecyclerView.adapter = albumItemAdapter
                popupButton.setOnClickListener { popupAlbums.invoke() }
            }

            "Artists" -> {
                lastFetchAction = { libraryViewModel.fetchAllArtists() }
                musicListRecyclerView.adapter = artistItemAdapter
                popupButton.setOnClickListener { popupArtists.invoke() }
            }

            "Playlists" -> {
                lastFetchAction = { libraryViewModel.fetchAllPlaylists() }
                musicListRecyclerView.adapter = playlistItemAdapter
                popupButton.setOnClickListener { popupPlaylists.invoke() }
            }
        }
        lastFetchAction.invoke()
    }

    private fun setUpMusicListRecyclerView(view: View) {
        setUpSongItemAdapter()
        setUpAlbumItemAdapter()
        setUpArtistItemAdapter()
        setUpPlaylistItemAdapter()
        musicListRecyclerView = view.findViewById(R.id.library_music_list_recyclerview)
        musicListRecyclerView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
    }

    private fun setUpPlaylistItemAdapter() {
        listenerManager.addListener(
            Playlist::class.java,
            object : ItemAdapterListenerInterface<Playlist> {
                override fun onItemClicked(item: Playlist, position: Int) {
                    onClickPlaylistItem(item)
                }

                override fun onItemLongClicked(item: Playlist, position: Int) =
                    // Handle song item long click
                    Unit
            })

        playlistItemAdapter = PlaylistItemAdapter(listenerManager)
    }

    private fun setUpArtistItemAdapter() {
        listenerManager.addListener(
            Artist::class.java,
            object : ItemAdapterListenerInterface<Artist> {
                override fun onItemClicked(item: Artist, position: Int) {
                    onClickArtistItem(item)
                }

                override fun onItemLongClicked(item: Artist, position: Int) {
                    // Handle song item long click
                }
            })

        artistItemAdapter = ArtistItemAdapter(listenerManager)
    }

    private fun setUpAlbumItemAdapter() {
        listenerManager.addListener(
            Album::class.java,
            object : ItemAdapterListenerInterface<Album> {
                override fun onItemClicked(item: Album, position: Int) {
                    onClickAlbumItem(item)
                }

                override fun onItemLongClicked(item: Album, position: Int) {
                    // Handle song item long click
                }
            })

        albumItemAdapter = AlbumItemAdapter(listenerManager)
    }

    private fun setUpSongItemAdapter() {
        listenerManager.addListener(
            Song::class.java,
            object : ItemAdapterListenerInterface<Song> {
                override fun onItemClicked(item: Song, position: Int) {
                    onClickSongItem(item, position)
                    Log.d("LibraryFragment", "onItemClicked: ${item.title} and position: $position")
                }

                override fun onItemLongClicked(item: Song, position: Int) {
                    // Handle song item long click
                }
            })

        songItemAdapter = SongItemAdapter(listenerManager)
    }

    private fun onClickSongItem(song: Song?, position: Int) {
        Log.d("LibraryFragment", "onClickSongItem: ${song?.title}")

        // TODO: PRETTY DANGEROUS
        var intent = Intent(context, SongPlayerActivity::class.java)
        intent.putExtra(INTENT_KEY_NEW_SONG_LIST, ArrayList(songItemAdapter!!.itemList))
        intent.putExtra(INTENT_KEY_SONG_INDEX, position)
        intent.action = ACTION_PLAY_NEW_SONG
        startActivity(intent)
    }

    private fun onClickAlbumItem(album: Album?) {
        Log.d("LibraryFragment", "onClickAlbumItem: ${album?.albumName}")
        lastFetchAction = { libraryViewModel.fetchSongsInAlbum(album!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsInAlbum.invoke(album!!) }
    }

    private fun onClickArtistItem(artist: Artist?) {
        Log.d("LibraryFragment", "onClickArtistItem: ${artist?.artistName}")
        lastFetchAction = { libraryViewModel.fetchSongsOfArtist(artist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsOfArtist.invoke(artist!!) }
    }

    private fun onClickPlaylistItem(playlist: Playlist?) {
        Log.d("LibraryFragment", "onClickPlaylistItem: ${playlist?.playlistName}")
        lastFetchAction = { libraryViewModel.fetchSongsInPlaylist(playlist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsInPlaylist.invoke(playlist!!) }
    }

    private fun setUpPopupMenu(view: View) {
        popupButton = view.findViewById(R.id.library_music_list_more_button)
        popupSongs = {
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.song_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_title -> {
                        lastFetchAction = { libraryViewModel.fetchSongsOrderByTitle() }
                    }

                    R.id.popup_menu_sort_by_artist -> {
                        lastFetchAction = { libraryViewModel.fetchSongsOrderByArtistNames() }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupSongsInAlbum = { album ->
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.song_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_title -> {
                        lastFetchAction = { libraryViewModel.fetchSongsInAlbumOrderByTitle(album) }
                    }

                    R.id.popup_menu_sort_by_artist -> {
                        lastFetchAction =
                            { libraryViewModel.fetchSongsInAlbumOrderByArtistNames(album) }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupSongsOfArtist = { artist ->
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.song_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_title -> {
                        lastFetchAction =
                            { libraryViewModel.fetchSongsOfArtistOrderByTitle(artist) }
                    }

                    R.id.popup_menu_sort_by_artist -> {
                        lastFetchAction = { libraryViewModel.fetchSongsOfArtist(artist) }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupSongsInPlaylist = { playlist ->
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.song_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_title -> {
                        lastFetchAction =
                            { libraryViewModel.fetchSongsInPlaylistOrderByTitle(playlist) }
                    }

                    R.id.popup_menu_sort_by_artist -> {
                        lastFetchAction =
                            { libraryViewModel.fetchSongsInPlaylistOrderByArtistNames(playlist) }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupAlbums = {
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.album_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_name -> {
                        lastFetchAction = { libraryViewModel.fetchAlbumsOrderByName() }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupArtists = {
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.artist_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_name -> {
                        lastFetchAction = { libraryViewModel.fetchArtistsOrderByName() }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
        popupPlaylists = {
            val popupMenu = PopupMenu(requireContext(), popupButton)
            popupMenu.menuInflater.inflate(R.menu.playlist_sort_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_menu_sort_by_name -> {
                        lastFetchAction = { libraryViewModel.fetchPlaylistsOrderByName() }
                    }
                }
                lastFetchAction.invoke()
                true
            }
            popupMenu.show()
        }
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