package com.example.cs426_magicmusic.ui.view.main.library

class ListenerManager {
    private val listeners = mutableMapOf<Class<*>, Any>()

    fun <T, L : ItemAdapterListenerInterface<T>> addListener(itemClass: Class<T>, listener: L) {
        listeners[itemClass] = listener
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getListener(itemClass: Class<T>): ItemAdapterListenerInterface<T>? {
        return listeners[itemClass] as? ItemAdapterListenerInterface<T>
    }
}