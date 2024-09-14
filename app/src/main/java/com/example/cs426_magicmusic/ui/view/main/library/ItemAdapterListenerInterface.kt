package com.example.cs426_magicmusic.ui.view.main.library

import android.widget.ImageButton

interface ItemAdapterListenerInterface<T> {
    fun onItemClicked(item: T, position: Int)
    fun onItemLongClicked(item: T, position: Int)
    fun onItemMenuClicked(imageButton: ImageButton, item: T, position: Int)
}
