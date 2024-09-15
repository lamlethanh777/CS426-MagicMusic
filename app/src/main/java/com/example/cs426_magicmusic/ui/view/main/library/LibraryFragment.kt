package com.example.cs426_magicmusic.ui.view.main.library

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.example.cs426_magicmusic.others.Constants.STRING_ALL_ALBUMS
import com.example.cs426_magicmusic.others.Constants.STRING_ALL_ARTISTS
import com.example.cs426_magicmusic.others.Constants.STRING_ALL_PLAYLISTS
import com.example.cs426_magicmusic.others.Constants.STRING_ALL_SONGS
import com.example.cs426_magicmusic.ui.view.songplayer.SongPlayerActivity
import com.example.cs426_magicmusic.ui.viewmodel.GenericViewModelFactory
import com.example.cs426_magicmusic.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {
    private var songItemAdapter: SongItemAdapter? = null
    private var albumItemAdapter: AlbumItemAdapter? = null
    private var artistItemAdapter: ArtistItemAdapter? = null
    private var playlistItemAdapter: PlaylistItemAdapter? = null
    private var displayOptionAdapter: DisplayOptionAdapter? = null
    private var listenerManager = ListenerManager()
    private var currentPlaylist: Playlist? = null

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
    private lateinit var viewButton: ImageButton
    private lateinit var ascendingOrderButton: ImageButton
    private lateinit var listNumber: TextView
    private lateinit var listTitle: TextView
    private lateinit var addPlaylistButton: ImageButton
    private lateinit var addSongToPlaylistButton: ImageButton

    private fun initViewModel() {
        val appDatabase = AppDatabase.getDatabase(requireContext())
        val songRepository = SongRepository(appDatabase)
        val albumRepository = AlbumRepository(appDatabase)
        val artistRepository = ArtistRepository(appDatabase)
        val playlistRepository = PlaylistRepository(appDatabase)

        LocalDBSynchronizer.setupRepositories(
            albumRepository, artistRepository, songRepository, playlistRepository
        )

        val factory = GenericViewModelFactory(LibraryViewModel::class.java) {
            LibraryViewModel(songRepository, albumRepository, artistRepository, playlistRepository)
        }
        libraryViewModel = ViewModelProvider(this, factory)[LibraryViewModel::class.java]
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
        viewButton = view.findViewById(R.id.library_music_list_toggle)
        listNumber = view.findViewById(R.id.library_music_list_number)
        ascendingOrderButton = view.findViewById(R.id.library_music_order_list_button)
        listTitle = view.findViewById(R.id.library_music_list_title)
        addPlaylistButton = view.findViewById(R.id.library_playlist_button)
        addSongToPlaylistButton = view.findViewById(R.id.library_add_song_playlist_button)

        setUpAddSongToPlaylistButton()
        setUpAddPlaylistButton()
        setUpPopupMenu(view)
        setUpMusicListRecyclerView(view)
        setUpDisplayOptionRecyclerView(view)
        setUpOrderButton()
        setUpToggleButton(view)
        subscribeToObservers()
        setClickListeners()
    }

    private fun setUpAddSongToPlaylistButton() {
        addSongToPlaylistButton.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_song_to_playlist, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val textView = dialogView.findViewById<TextView>(R.id.add_song_playlist_name)
            val searchView = dialogView.findViewById<SearchView>(R.id.add_song_search_view)
            val recyclerView = dialogView.findViewById<RecyclerView>(R.id.add_song_recycler_view)
            val addButton = dialogView.findViewById<Button>(R.id.add_song_button)
            val cancelButton = dialogView.findViewById<Button>(R.id.add_song_cancel_button)

            val placeholder = "Add song to playlist:\n${currentPlaylist!!.playlistName}"
            textView.text = placeholder

            val selectedSongs = mutableListOf<Song>()
            val songAdapter = SearchSongPlaylistAdapter { song ->
                if (!selectedSongs.contains(song)) {
                    selectedSongs.add(song)
                    Toast.makeText(requireContext(), "Adding ${song.title}", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = songAdapter

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
//                    query?.let {
//                        libraryViewModel.filterSongs(it)
//                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        libraryViewModel.filterSongs(it)
                    }
                    return true
                }
            })

            addButton.setOnClickListener {// Replace with actual playlist name
                libraryViewModel.addMultipleSongsToPlaylist(currentPlaylist!!, selectedSongs)
                Toast.makeText(
                    requireContext(),
                    "Added ${selectedSongs.size} songs to ${currentPlaylist!!.playlistName}",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                Toast.makeText(
                    requireContext(), "Adding songs cancelled", Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }

            libraryViewModel.filteredSongs.observe(viewLifecycleOwner) { songs ->
                songAdapter.submitList(songs)
            }

            dialog.show()
        }
    }

    private fun setUpAddPlaylistButton() {
        addPlaylistButton.setOnClickListener {
            showCreatePlaylistDialog()
        }
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
        var placeholder: String
        libraryViewModel.songs.observe(viewLifecycleOwner) {
            songItemAdapter?.itemList = it
            placeholder = "${it.size} songs"
            listNumber.text = placeholder
            musicListRecyclerView.scrollToPosition(0)
        }
        libraryViewModel.albums.observe(viewLifecycleOwner) {
            albumItemAdapter?.itemList = it
            placeholder = "${it.size} albums"
            listNumber.text = placeholder
            musicListRecyclerView.scrollToPosition(0)
        }
        libraryViewModel.artists.observe(viewLifecycleOwner) {
            artistItemAdapter?.itemList = it
            placeholder = "${it.size} artists"
            listNumber.text = placeholder
            musicListRecyclerView.scrollToPosition(0)
        }
        libraryViewModel.playlists.observe(viewLifecycleOwner) {
            playlistItemAdapter?.itemList = it
            placeholder = "${it.size} playlists"
            listNumber.text = placeholder
            musicListRecyclerView.scrollToPosition(0)
        }

        libraryViewModel.currentLayout.observe(viewLifecycleOwner) {
            if (it == TemplateItemAdapter.LayoutType.GRID) {
                musicListRecyclerView.layoutManager = GridLayoutManager(
                    context, 2, GridLayoutManager.VERTICAL, false
                )
                songItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                albumItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                artistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                playlistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.GRID
                viewButton.setImageResource(R.drawable.ic_list_view)
            } else {
                musicListRecyclerView.layoutManager = LinearLayoutManager(
                    context, LinearLayoutManager.VERTICAL, false
                )
                songItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                albumItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                artistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                playlistItemAdapter?.layoutType = TemplateItemAdapter.LayoutType.LIST
                viewButton.setImageResource(R.drawable.ic_grid_view)
            }
            musicListRecyclerView.scrollToPosition(0)
        }

        libraryViewModel.ascendingOrder.observe(viewLifecycleOwner) {
            Log.d("LibraryFragment", "reversed")
            songItemAdapter?.itemList = songItemAdapter?.itemList?.reversed()!!
            albumItemAdapter?.itemList = albumItemAdapter?.itemList?.reversed()!!
            artistItemAdapter?.itemList = artistItemAdapter?.itemList?.reversed()!!
            playlistItemAdapter?.itemList = playlistItemAdapter?.itemList?.reversed()!!
            musicListRecyclerView.scrollToPosition(0)
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

    private fun setUpOrderButton() {
        ascendingOrderButton.setOnClickListener {
            libraryViewModel.toggleCurrentOrder()
        }
    }

    private fun setUpToggleButton(view: View) {
        view.findViewById<ImageButton>(R.id.library_music_list_toggle)
            .setOnClickListener {
                libraryViewModel.toggleCurrentLayout()
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
                listTitle.text = STRING_ALL_SONGS
                addPlaylistButton.visibility = View.INVISIBLE
                addSongToPlaylistButton.visibility = View.INVISIBLE
                currentPlaylist = null
            }

            "Albums" -> {
                lastFetchAction = { libraryViewModel.fetchAllAlbums() }
                musicListRecyclerView.adapter = albumItemAdapter
                popupButton.setOnClickListener { popupAlbums.invoke() }
                listTitle.text = STRING_ALL_ALBUMS
                addPlaylistButton.visibility = View.INVISIBLE
                addSongToPlaylistButton.visibility = View.INVISIBLE
                currentPlaylist = null
            }

            "Artists" -> {
                lastFetchAction = { libraryViewModel.fetchAllArtists() }
                musicListRecyclerView.adapter = artistItemAdapter
                popupButton.setOnClickListener { popupArtists.invoke() }
                listTitle.text = STRING_ALL_ARTISTS
                addPlaylistButton.visibility = View.INVISIBLE
                addSongToPlaylistButton.visibility = View.INVISIBLE
                currentPlaylist = null
            }

            "Playlists" -> {
                lastFetchAction = { libraryViewModel.fetchAllPlaylists() }
                musicListRecyclerView.adapter = playlistItemAdapter
                popupButton.setOnClickListener { popupPlaylists.invoke() }
                listTitle.text = STRING_ALL_PLAYLISTS
                addPlaylistButton.visibility = View.VISIBLE
                addSongToPlaylistButton.visibility = View.INVISIBLE
                currentPlaylist = null
            }
        }
        lastFetchAction.invoke()
    }

    private fun showCreatePlaylistDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val playlistNameInput = dialogView.findViewById<EditText>(R.id.playlist_name_input)
        val createButton = dialogView.findViewById<Button>(R.id.create_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)

        createButton.setOnClickListener {
            val playlistName = playlistNameInput.text.toString()
            if (playlistName.isNotEmpty()) {
                libraryViewModel.addNewPlaylist(playlistName)
            }
            Toast.makeText(
                requireContext(), "Created playlist: $playlistName", Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

                override fun onItemLongClicked(item: Playlist, position: Int) = Unit

                override fun onItemMenuClicked(
                    imageButton: ImageButton, item: Playlist, position: Int
                ) {
                    showPlaylistMenu(imageButton, item)
                }
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

                override fun onItemLongClicked(item: Artist, position: Int) = Unit

                override fun onItemMenuClicked(
                    imageButton: ImageButton, item: Artist, position: Int
                ) = Unit
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

                override fun onItemLongClicked(item: Album, position: Int) = Unit

                override fun onItemMenuClicked(
                    imageButton: ImageButton, item: Album, position: Int
                ) = Unit
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

                override fun onItemMenuClicked(
                    imageButton: ImageButton, item: Song, position: Int
                ) {
                    showSongInPlaylistMenu(imageButton, item)
                }
            })

        songItemAdapter = SongItemAdapter(listenerManager)
    }

    private fun onClickSongItem(song: Song?, position: Int) {
        Log.d("LibraryFragment", "onClickSongItem: ${song?.title}")

        // TODO: PRETTY DANGEROUS
        val intent = Intent(context, SongPlayerActivity::class.java)
        intent.putExtra(INTENT_KEY_NEW_SONG_LIST, ArrayList(songItemAdapter!!.itemList))
        intent.putExtra(INTENT_KEY_SONG_INDEX, position)
        intent.action = ACTION_PLAY_NEW_SONG
        startActivity(intent)
    }

    private fun onClickAlbumItem(album: Album?) {
        Log.d("LibraryFragment", "onClickAlbumItem: ${album?.albumName}")
        val placeholder = "Album: ${album?.albumName}"
        listTitle.text = placeholder
        lastFetchAction = { libraryViewModel.fetchSongsInAlbum(album!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsInAlbum.invoke(album!!) }
    }

    private fun onClickArtistItem(artist: Artist?) {
        Log.d("LibraryFragment", "onClickArtistItem: ${artist?.artistName}")
        val placeholder = "Artist: ${artist?.artistName}"
        listTitle.text = placeholder
        lastFetchAction = { libraryViewModel.fetchSongsOfArtist(artist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsOfArtist.invoke(artist!!) }
    }

    private fun onClickPlaylistItem(playlist: Playlist?) {
        Log.d("LibraryFragment", "onClickPlaylistItem: ${playlist?.playlistName}")
        val placeholder = "Playlist: ${playlist?.playlistName}"
        listTitle.text = placeholder
        lastFetchAction = { libraryViewModel.fetchSongsInPlaylist(playlist!!) }
        musicListRecyclerView.adapter = songItemAdapter
        lastFetchAction.invoke()
        popupButton.setOnClickListener { popupSongsInPlaylist.invoke(playlist!!) }
        currentPlaylist = playlist
        addSongToPlaylistButton.visibility = View.VISIBLE
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

    private fun showSongInPlaylistMenu(menuButton: ImageButton, song: Song) {
        if (currentPlaylist == null) {
            return
        }

        val popupMenu = PopupMenu(requireContext(), menuButton)
        popupMenu.menuInflater.inflate(R.menu.song_in_playlist_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.song_playlist_popup_menu_remove -> {
                    libraryViewModel.removeSongFromPlaylist(currentPlaylist!!, song)
                    Toast.makeText(
                        requireContext(),
                        "Removed ${song.title} from ${currentPlaylist!!.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun showPlaylistMenu(menuButton: ImageButton, playlist: Playlist) {
        val popupMenu = PopupMenu(requireContext(), menuButton)
        popupMenu.menuInflater.inflate(R.menu.playlist_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.playlist_popup_menu_delete -> {
                    libraryViewModel.deletePlaylist(playlist)
                    Toast.makeText(
                        requireContext(),
                        "Deleted playlist: ${playlist.playlistName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            true
        }
        popupMenu.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = LibraryFragment()
    }
}