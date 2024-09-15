package com.example.cs426_magicmusic.ui.view.songplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.others.Constants.BUNDLE_KEY_LYRIC
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LyricBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var lyricTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_lyric_bottom_sheet, container, false)
        lyricTextView = view.findViewById(R.id.lyric_textview)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(BUNDLE_KEY_LYRIC)?.let {
            lyricTextView.text = it
        }
    }

    companion object {
        fun newInstance(lyrics: String): LyricBottomSheetFragment {
            val fragment = LyricBottomSheetFragment()
            val args = Bundle()
            args.putString(BUNDLE_KEY_LYRIC, lyrics)
            fragment.arguments = args
            return fragment
        }
    }
}