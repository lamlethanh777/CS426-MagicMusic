package com.example.cs426_magicmusic.utils

import android.content.Context
import android.net.Uri
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.cs426_magicmusic.R

/**
 * Utility class for loading images with Glide
 */

object ImageUtility {
    fun loadImage(context: Context, songUri: String, view: ImageView) {
        val thumbnail = try {
            context.contentResolver.loadThumbnail(
                Uri.parse(songUri), Size(350, 350), null
            )
        } catch (e: Exception) {
            null // Handle any exceptions (e.g., no thumbnail found)
        }

        if (thumbnail == null) {
            Glide.with(context)
                .load(R.drawable.placeholder_default)  // Placeholder image while loading
                .into(view)
        } else {
            Glide.with(context)
                .load(thumbnail)
                .placeholder(R.drawable.placeholder_default)  // Placeholder image while loading
                .error(R.drawable.placeholder_error)       // Image to display if the load fails
                .into(view)
        }
    }
}
