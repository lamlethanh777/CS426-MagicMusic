package com.example.cs426_magicmusic

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryFragment : Fragment() {
    interface ButtonListeners {
        fun LibraryFragment_onMusicItemButton(musicItem: MusicItem)
    }
    var buttonListeners: ButtonListeners? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_library, container, false)

        val musicItemList = MusicRepository.getMusicList("all")
        val musicListRecyclerView: RecyclerView = view.findViewById(R.id.library_music_list_recycleview)
        val musicListAdapter = MusicItemListAdapter(musicItemList)
        musicListRecyclerView.setAdapter(musicListAdapter)
        musicListRecyclerView.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
        musicListAdapter.setOnButtonClick(object : MusicItemListAdapter.ButtonListeners {
            override fun MusicItemListAdapter_onClick(musicItem: MusicItem) {
                buttonListeners?.LibraryFragment_onMusicItemButton(musicItem)
            }
        })

        return view
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ButtonListeners) {
            buttonListeners = context
        } else {
            throw RuntimeException("$context must implement TransportFlightsFragment.ButtonListeners")
        }
    }
}