package com.example.cs426_magicmusic.ui.view.main

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
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_ARTIST
import com.example.cs426_magicmusic.utils.ImageUtility

class SongItemAdapter(
    private val itemListener: ((Song?) -> Unit)
) : RecyclerView.Adapter<SongItemAdapter.SongViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }

    private val differ: AsyncListDiffer<Song> = AsyncListDiffer(this, diffCallback)

    var songList: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_song_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.onBind(songList[position])
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var image: ImageView = itemView.findViewById<ImageView>(R.id.item_song_image)
        private var title: TextView = itemView.findViewById<TextView>(R.id.item_song_title)
        private var artists: TextView = itemView.findViewById<TextView>(R.id.item_song_artists)
        private var mSong: Song? = null

        fun onBind(song: Song) {
            mSong = song
            title.text = song.title
            artists.text = song.artistNames
            ImageUtility.loadImage(itemView.context, song.uri, image)

            itemView.setOnClickListener{ itemListener(mSong) }
        }
    }
}
