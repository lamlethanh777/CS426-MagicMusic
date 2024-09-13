package com.example.cs426_magicmusic.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ScrollView
import androidx.cardview.widget.CardView
import androidx.palette.graphics.Palette
import com.example.cs426_magicmusic.R

object PaletteUtility {

    /**
     * Extracts dominant colors from the image and applies them to corresponding UI elements.
     */
    fun applyPaletteFromImage(
        context: Context,
        bitmap: Bitmap,
        innerLayout: CardView,
        outerLayout: ScrollView
    ) {
//        val bitmap: Bitmap = imageView.drawable.toBitmap()

        Palette.from(bitmap).generate { palette ->
            palette?.let {
//                val dominantColor = palette.getDominantColor(context.getColor(R.color.white))
                val mutedColor = palette.getMutedColor(0)
                val lightMutedColor = palette.getLightMutedColor(mutedColor)
                val darkMutedColor = palette.getDarkMutedColor(lightMutedColor)
                val vibrantColor = palette.getVibrantColor(0)
                val lightVibrantColor = palette.getLightVibrantColor(0)
                val darkVibrantColor= palette.getDarkVibrantColor(0)
                val dominantColor= palette.getDominantColor(0)

                var innerLayoutColor = lightMutedColor
                var outerLayoutColor = mutedColor

                if (innerLayoutColor == 0 || outerLayoutColor == 0 || innerLayoutColor == outerLayoutColor) {
                    // This means the Palette didn't find a dominant color, and it's using white.
                    innerLayoutColor = context.getColor(R.color.link_water)
                    outerLayoutColor = context.getColor(R.color.cherub)
                    Log.d("Color", "Falling back to default color")
                } else {
                    Log.d("Color", "Using the dominant color from the image")
                }

                // Set the background color of the layout
                innerLayout.setCardBackgroundColor(innerLayoutColor)
                outerLayout.setBackgroundColor(outerLayoutColor)
            }
        }
    }

}
