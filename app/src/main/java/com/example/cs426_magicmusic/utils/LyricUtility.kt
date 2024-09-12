package com.example.cs426_magicmusic.utils

import android.os.Environment
import android.util.Log
import com.example.cs426_magicmusic.others.Constants
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_LYRIC
import java.io.File

object LyricUtility {
    fun loadLyric(songName: String): String {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "magicmusic/lyric"
        )
        val files = downloadsDir.listFiles()
        var filePath: String? = null

        if (files != null && files.isNotEmpty()) {
            filePath = files.find { it.nameWithoutExtension.equals(songName, ignoreCase = true) }?.toString()
        }

        if (filePath == null) {
            Log.d("No file", "No lyric file found")
            return STRING_UNKNOWN_LYRIC
        }

        val file = File(filePath)
        return if (file.exists()) {
            file.readText()
        } else {
            Log.d("No file", file.absolutePath)
            Constants.STRING_UNKNOWN_LYRIC
        }
    }
}
