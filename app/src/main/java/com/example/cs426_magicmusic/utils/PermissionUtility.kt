package com.example.cs426_magicmusic.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.example.cs426_magicmusic.others.Constants.REQUEST_CODE_PERMISSION
import com.vmadalin.easypermissions.EasyPermissions

/**
 * Utility class for handling permissions
 */

object PermissionUtility {

    private val permissions = mutableListOf(
        Manifest.permission.FOREGROUND_SERVICE
    )

    fun hasEnoughPermission(context: Context): Boolean {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }

        return EasyPermissions.hasPermissions(context, *permissions.toTypedArray())
    }

    fun requestPermissions(activity: AppCompatActivity) {
        if (hasEnoughPermission(activity)) {
            return
        }

        EasyPermissions.requestPermissions(
            activity,
            "This application cannot work without external storage permission.",
            REQUEST_CODE_PERMISSION,
            *permissions.toTypedArray()
        )
    }
}
