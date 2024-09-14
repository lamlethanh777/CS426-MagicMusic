package com.example.cs426_magicmusic.utils

import android.os.Environment
import android.util.Log
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_LYRIC
import org.json.JSONObject
import java.io.File

object LyricUtility {
    fun loadLyricFromJson(songName: String): String {
        val downloadsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "magicmusic/metadata"
        )
        val files = downloadsDir.listFiles()
        var filePath: String? = null

        // Search for the correct JSON file in the directory based on the song name
        if (files != null && files.isNotEmpty()) {
            filePath = files.find { it.nameWithoutExtension.equals(songName, ignoreCase = true) }?.toString()
        }

        if (filePath == null) {
            Log.d("No file", "No JSON file found for the song: $songName")
            return STRING_UNKNOWN_LYRIC // Replace with your constant for unknown lyric
        }

        try {
            val file = File(filePath)
            if (file.exists()) {
                // Read the JSON file content
                val jsonString = file.readText()

                // Parse the JSON object and extract the lyric field
                val jsonObject = JSONObject(jsonString)
                val title = jsonObject.optString("title", "Unknown Title") // Extract "title" from JSON
                val lyric = jsonObject.optString("lyric", STRING_UNKNOWN_LYRIC)

                // Format the lyric
                val formattedLyric = lyric
                    .replace("\\n", System.lineSeparator())  // Replace \n with a new line
                    .replace("[", System.lineSeparator() + "[")  // Format for stanza/paragraphs

                val returnContent = "$title${System.lineSeparator()}${formattedLyric ?: ""}"

                return returnContent
            } else {
                Log.d("No file", file.absolutePath)
                return STRING_UNKNOWN_LYRIC
            }
        } catch (e: Exception) {
            Log.e("Error", "Error reading or parsing the JSON file", e)
            return STRING_UNKNOWN_LYRIC
        }
    }
}
