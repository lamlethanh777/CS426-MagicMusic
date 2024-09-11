package com.example.cs426_magicmusic.utils

import java.util.Locale

/**
 * Utility class for formatting time
 */

object TimeFormatUtility {
    fun formatTimestampToMMSS(timestamp: Int): String {
        val minutes = timestamp / 1000 / 60
        val seconds = (timestamp / 1000) % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
