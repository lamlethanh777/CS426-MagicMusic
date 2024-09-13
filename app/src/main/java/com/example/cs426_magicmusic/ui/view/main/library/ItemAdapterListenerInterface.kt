package com.example.cs426_magicmusic.ui.view.main.library

interface ItemAdapterListenerInterface<T> {
    fun onItemClicked(item: T, position: Int)
    fun onItemLongClicked(item: T, position: Int)
}
