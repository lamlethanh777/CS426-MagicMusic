package com.example.cs426_magicmusic.ui.view.main

interface AdapterItemListenerInterface<T> {
    fun onItemClicked(item: T, position: Int)
    fun onItemLongClicked(item: T, position: Int)
}
