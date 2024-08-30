package com.example.cs426_magicmusic

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class MusicItemListAdapter(private var dataList: List<MusicItem>) :
    RecyclerView.Adapter<MusicItemListAdapter.ViewHolder>() {
    interface ButtonListeners {
        fun MusicItemListAdapter_onClick(musicItem: MusicItem)
    }

    private var buttonListeners: ButtonListeners? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setOnButtonClick(buttonListeners: ButtonListeners?) {
        this.buttonListeners = buttonListeners
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_music_item_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val musicItem: MusicItem = dataList[position]

        holder.icon.setBackgroundResource(musicItem.icon)
        holder.title.text = musicItem.title
        holder.author.text = musicItem.author.joinToString(", ")

        if (buttonListeners != null) {
            holder.layout.setOnClickListener {
                buttonListeners!!.MusicItemListAdapter_onClick(musicItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById<View>(R.id.item_music_icon) as ImageView
        var title: TextView = itemView.findViewById<View>(R.id.item_music_title) as TextView
        var author: TextView = itemView.findViewById<View>(R.id.item_music_author) as TextView
        var layout: ConstraintLayout = itemView.findViewById<View>(R.id.item_music_layout) as ConstraintLayout
    }
}
