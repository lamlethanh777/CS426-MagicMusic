package com.example.cs426_magicmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.cs426_magicmusic.R

/**
 * Utility class for loading images with Glide
 */

object ImageUtility {
    fun loadImage(context: Context, songUri: String, view: ImageView) {
        val thumbnail = loadBitmap(context, songUri)

        if (thumbnail == null) {
            Glide.with(context)
                .load(R.drawable.ic_music_note)  // Placeholder image while loading
                .into(view)
        } else {
            Glide.with(context)
                .load(thumbnail)
                .placeholder(R.drawable.placeholder_default)  // Placeholder image while loading
                .error(R.drawable.placeholder_error)       // Image to display if the load fails
                .into(view)
        }
    }

    fun loadBitmap(context: Context, uri: String): Bitmap? = try {
        context.contentResolver.loadThumbnail(
            Uri.parse(uri), Size(350, 350), null
        )
    } catch (e: Exception) {
        Log.e("ImageUtility", "Exception occurred: ${e.message}")
        BitmapFactory.decodeResource(context.resources, R.drawable.placeholder_default)?.also {
            if (it == null) {
                Log.e("ImageUtility", "Failed to load placeholder image")
            }
        }
    }
}
