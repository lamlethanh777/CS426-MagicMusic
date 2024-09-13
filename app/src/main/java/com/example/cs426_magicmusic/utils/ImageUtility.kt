package com.example.cs426_magicmusic.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.transition.Transition
import android.util.Log
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.example.cs426_magicmusic.R
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_IMAGE
import com.example.cs426_magicmusic.others.Constants.STRING_UNKNOWN_LYRIC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * Utility class for loading images with Glide
 */

object ImageUtility {
    fun loadImageFromUri(context: Context, songUri: String, view: ImageView) {
        val thumbnail = loadBitmap(context, songUri)

        if (thumbnail == null) {
            Glide.with(context)
                .load(R.drawable.ic_music_note)  // Placeholder image while loading
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache the original and resized versions
                .into(view)
        } else {
            Glide.with(context)
                .load(thumbnail)
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache the original and resized versions
                .placeholder(R.drawable.placeholder_default)  // Placeholder image while loading
                .error(R.drawable.placeholder_error)       // Image to display if the load fails
                .into(view)
        }
    }

    fun loadImageFromUrl(context: Context, imageUrl: String, view: ImageView) {
        Glide.with(context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache the original and resized versions
            .placeholder(R.drawable.placeholder_default)  // Placeholder image while loading
            .error(R.drawable.placeholder_error)  // Error image if loading fails
            .into(view)
    }

    fun loadBitmap(context: Context, uri: String): Bitmap? = try {
        context.contentResolver.loadThumbnail(
            Uri.parse(uri), Size(350, 350), null
        )
    } catch (e: Exception) {
        Log.d("ImageUtility", "Exception occurred: ${e.message}")
        BitmapFactory.decodeResource(context.resources, R.drawable.placeholder_default)?.also {
            if (it == null) {
                Log.e("ImageUtility", "Failed to load placeholder image")
            }
        }
    }

    fun loadImageUrlFromJson(songName: String): String {
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
            return STRING_UNKNOWN_IMAGE // Replace with your constant for unknown lyric
        }

        try {
            val file = File(filePath)
            if (file.exists()) {
                // Read the JSON file content
                val jsonString = file.readText()

                // Parse the JSON object and extract the lyric field
                val jsonObject = JSONObject(jsonString)
                val imageUrl = jsonObject.optString("image_url", STRING_UNKNOWN_IMAGE)

                return imageUrl
            } else {
                Log.d("No image file", file.absolutePath)
                return STRING_UNKNOWN_IMAGE
            }
        } catch (e: Exception) {
            Log.e("Error", "Error reading or parsing the JSON file", e)
            return STRING_UNKNOWN_IMAGE
        }
    }

    /**
     * If songName has imageUrl, load from Url; otherwise load from songUri
     *
     * @param context The context used to access external storage.
     * @param songName Name of song without extension.
     * @param songUri Song uri.
     * @param view ImageView
     * @return Image is loaded to view
     */
    fun loadImage(context: Context, songName: String, songUri: String, view: ImageView) {
        val imageUrl = loadImageUrlFromJson(songName)
        if (imageUrl == STRING_UNKNOWN_IMAGE)
            loadImageFromUri(context, songUri, view)
        else
            loadImageFromUrl(context, imageUrl, view)
    }

    fun getBitmapFromImageView(view: ImageView): Bitmap? {
        val drawable = view.drawable
        return when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            else -> null
        }
    }

    suspend fun loadBitmapFromUrl(context: Context, songName: String): Bitmap? {
        val url = loadImageUrlFromJson(songName)
//        return Glide.with(context)
//                .asBitmap()
//                .load(url)
//                .submit().get()
//        }

        return withContext(Dispatchers.IO) {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .submit()
                    .get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}
