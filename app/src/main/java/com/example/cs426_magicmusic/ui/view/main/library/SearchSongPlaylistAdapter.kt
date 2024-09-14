package com.example.cs426_magicmusic.ui.view.main.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.utils.ImageUtility

class SearchSongPlaylistAdapter(private val onSongSelected: (Song) -> Unit) :
    ListAdapter<Song, SearchSongPlaylistAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_searched_song_layout, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.onBind(song)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var _image = itemView.findViewById<ImageView>(R.id.item_song_image)
        private var _title = itemView.findViewById<TextView>(R.id.item_song_title)
        private var _artists = itemView.findViewById<TextView>(R.id.item_song_artists)

        fun onBind(song: Song) {
            _title.text = song.title
            _artists.text = song.artistNames
            ImageUtility.loadImage(itemView.context, song.title, song.uri, _image)

            itemView.setOnClickListener {
                onSongSelected(song)
            }
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}