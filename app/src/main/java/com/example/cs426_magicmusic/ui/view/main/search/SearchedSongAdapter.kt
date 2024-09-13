package com.example.cs426_magicmusic.ui.view.main.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.ui.view.main.library.ItemAdapterListenerInterface
import com.example.cs426_magicmusic.utils.ImageUtility

class SearchedSongAdapter(
    private val itemListener: (Song, Int) -> Unit
) : RecyclerView.Adapter<SearchedSongAdapter.SongViewHolder>() {
    object DiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }

    private val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, DiffCallback)

    var itemList: List<Song>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.adapter_searched_song_layout, parent,
            false
        )
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.onBind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var _image = itemView.findViewById<ImageView>(R.id.item_song_image)
        private var _title = itemView.findViewById<TextView>(R.id.item_song_title)
        private var _artists = itemView.findViewById<TextView>(R.id.item_song_artists)

        fun onBind(song: Song) {
            _title.text = song.title
            _artists.text = song.artistNames
            ImageUtility.loadImage(itemView.context, song.title, song.uri, _image)

            itemView.setOnClickListener { itemListener(song, absoluteAdapterPosition) }

            Log.d("SearchedSongAdapter", "onBind2: ${song.title} at $absoluteAdapterPosition")
        }
    }
}