package com.example.cs426_magicmusic.ui.view.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R

class DisplayOptionAdapter(
    private val displayOptionList: List<String>,
    private val itemListener: ((String) -> Unit)
) : RecyclerView.Adapter<DisplayOptionAdapter.DisplayOptionViewHolder>() {

    private var current_selected = -1
    init {
        if (displayOptionList.isNotEmpty()) {
            itemListener(displayOptionList[0])
            setSelection(0)
        }
    }

    fun getDisplayOptionList(): List<String> {
        return displayOptionList
    }

    override fun getItemCount(): Int {
        return displayOptionList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisplayOptionViewHolder {
        return DisplayOptionViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.adapter_button_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: DisplayOptionViewHolder, position: Int) {
        holder.onBind(displayOptionList[position], position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(position: Int) {
        if (current_selected >= 0) {
            notifyItemChanged(current_selected)
        }
        current_selected = position
        notifyItemChanged(current_selected)
    }

    inner class DisplayOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var button: Button = itemView.findViewById<Button>(R.id.item_button)

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(str: String, position: Int) {
            button.text = str
            if (position == current_selected) {
                button.backgroundTintList = itemView.resources.getColorStateList(R.color.active_button_background)
            }
            else {
                button.backgroundTintList = itemView.resources.getColorStateList(R.color.default_button_background)
            }
            itemView.setOnClickListener{ itemListener(str); setSelection(position) }
        }
    }
}
