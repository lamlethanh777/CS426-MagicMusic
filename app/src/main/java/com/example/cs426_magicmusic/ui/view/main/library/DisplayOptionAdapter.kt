package com.example.cs426_magicmusic.ui.view.main.library

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.TransitionDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cs426_magicmusic.R

class DisplayOptionAdapter(
    private val displayOptionList: List<String>,
    private val itemListener: ((String) -> Unit)
) : RecyclerView.Adapter<DisplayOptionAdapter.DisplayOptionViewHolder>() {

    private var currentSelected = -1

    init {
        if (displayOptionList.isNotEmpty()) {
            itemListener(displayOptionList[0])
            setSelection(0)
        }
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
        if (currentSelected >= 0) {
            notifyItemChanged(currentSelected)
        }
        currentSelected = position
        notifyItemChanged(currentSelected)
    }

    inner class DisplayOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var textView = itemView.findViewById<TextView>(R.id.item_text)

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun onBind(str: String, position: Int) {
            textView.text = str
            val transitionDrawable = textView.background as? TransitionDrawable
            if (position == currentSelected) {
                textView.setBackgroundResource(R.drawable.rounded_corners_chosen)
                transitionDrawable?.startTransition(1000)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_corners_idle)
                transitionDrawable?.reverseTransition(1000)
            }
            itemView.setOnClickListener { itemListener(str); setSelection(position) }
        }
    }
}
