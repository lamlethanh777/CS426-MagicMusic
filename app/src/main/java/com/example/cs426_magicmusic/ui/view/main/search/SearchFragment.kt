package com.example.cs426_magicmusic.ui.view.main.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R
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
import com.example.cs426_magicmusic.ui.view.song_player.SongPlayerActivity
import com.example.cs426_magicmusic.ui.viewmodel.GenericViewModelFactory
import com.example.cs426_magicmusic.ui.viewmodel.SearchViewModel

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchedSongAdapter: SearchedSongAdapter
    private lateinit var searchViewModel: SearchViewModel

    private fun initViewModel() {
        val appDatabase = AppDatabase.getDatabase(requireContext())
        val songRepository = SongRepository(appDatabase)
        val albumRepository = AlbumRepository(appDatabase)
        val artistRepository = ArtistRepository(appDatabase)
        val playlistRepository = PlaylistRepository(appDatabase)

        LocalDBSynchronizer.setupRepositories(
            albumRepository, artistRepository, songRepository, playlistRepository
        )

        val factory = GenericViewModelFactory(SearchViewModel::class.java) {
            SearchViewModel(songRepository)
        }
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.search_view)

        recyclerView = view.findViewById(R.id.search_results)

        searchedSongAdapter = SearchedSongAdapter(::onClickSongItem)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = searchedSongAdapter

        searchViewModel.filteredSongs.observe(viewLifecycleOwner) { filteredSongs ->
            searchedSongAdapter.itemList = filteredSongs
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
//                searchViewModel.filterSongs(query.toString())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.filterSongs(newText.toString())
                return true
            }
        })
    }

    private fun onClickSongItem(song: Song?, position: Int) {
        Log.d("SearchFragment", "onClickSongItem: ${song?.title}")

        val intent = Intent(context, SongPlayerActivity::class.java)
        intent.putExtra(
            INTENT_KEY_NEW_SONG_LIST,
            ArrayList(searchedSongAdapter.itemList)
        )
        intent.putExtra(INTENT_KEY_SONG_INDEX, position)
        intent.action = ACTION_PLAY_NEW_SONG
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SearchFragment().apply {
                // arguments = Bundle().apply {
                //     putString(ARG_PARAM1, param1)
                //     putString(ARG_PARAM2, param2)
                // }
            }
    }
}