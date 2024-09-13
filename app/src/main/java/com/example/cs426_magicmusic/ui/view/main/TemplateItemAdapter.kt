package com.example.cs426_magicmusic.ui.view.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.utils.ImageUtility

abstract class TemplateItemAdapter<T>(
    private val listenerManager: ListenerManager,
    private val diffCallback: DiffUtil.ItemCallback<T>
) : RecyclerView.Adapter<TemplateItemAdapter<T>.TemplateViewHolder>() {

    enum class LayoutType {
        GRID, LIST
    }

    private var _layoutType: LayoutType = LayoutType.LIST
    var layoutType: LayoutType
        get() = _layoutType
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            _layoutType = value
            notifyDataSetChanged()
        }

    private val differ: AsyncListDiffer<T> = AsyncListDiffer(this, diffCallback)

    var itemList: List<T>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (_layoutType) {
            LayoutType.LIST -> 0
            LayoutType.GRID -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        return when (_layoutType) {
            LayoutType.LIST -> {
                createListViewHolder(parent)
            }
            LayoutType.GRID -> {
                createGridViewHolder(parent)
            }
        }
    }

    private fun createGridViewHolder(parent: ViewGroup): TemplateItemAdapter<T>.TemplateViewHolder {
        return TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_song_item_grid,
                parent,
                false
            )
        )
    }

    private fun createListViewHolder(parent: ViewGroup): TemplateItemAdapter<T>.TemplateViewHolder {
        return TemplateViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_song_item_list,
                parent,
                false
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val item = itemList[position]
        val listener = listenerManager.getListener(item!!::class.java) as? AdapterItemListenerInterface<T>
        holder.onBind(item, getTitle(item), getSubtitle(item), getImageUri(item), position, listener)
    }

    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var _image = itemView.findViewById<ImageView>(R.id.item_song_image)
        private var _title = itemView.findViewById<TextView>(R.id.item_song_title)
        private var _artists = itemView.findViewById<TextView>(R.id.item_song_artists)

        fun onBind(
            item: T, title: String, artists: String, imageUri: String, position: Int,
            listener: AdapterItemListenerInterface<T>?
        ) {
            _title.text = title
            _artists.text = artists
            ImageUtility.loadImage(itemView.context, imageUri, _image)
            itemView.setOnClickListener { listener?.onItemClicked(item, position) }
            itemView.setOnLongClickListener {
                listener?.onItemLongClicked(item, position)
                true
            }
        }
    }

    abstract fun getTitle(item: Any?): String

    abstract fun getSubtitle(item: Any?): String

    abstract fun getImageUri(item: Any?): String
}
