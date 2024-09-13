package com.example.cs426_magicmusic.ui.view.main

class ListenerManager {
    private val listeners = mutableMapOf<Class<*>, Any>()

    fun <T, L : AdapterItemListenerInterface<T>> addListener(itemClass: Class<T>, listener: L) {
        listeners[itemClass] = listener
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getListener(itemClass: Class<T>): AdapterItemListenerInterface<T>? {
        return listeners[itemClass] as? AdapterItemListenerInterface<T>
    }
}