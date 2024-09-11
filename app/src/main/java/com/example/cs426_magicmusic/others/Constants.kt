package com.example.cs426_magicmusic.others

/**
 * Constants used in the app
 */

object Constants {
        const val RUNNING_DATABASE_NAME = "running_db"

        const val REQUEST_CODE_PERMISSION = 1

        const val DEFAULT_STARTING_AUDIO_POSITION = 0

        const val UPDATE_PLAYER_POSITION_INTERVAL = 100L

        /**
         * Music player actions
         */
        const val ACTION_PLAY_NEW_SONG = "ACTION_PLAY_NEW_SONG"

        const val ACTION_RETURN_TO_SONG_PLAYER_ACTIVITY = "ACTION_RETURN_TO_SONG_PLAYER_ACTIVITY"

        /**
         * Intent keys when sending data between activities
         */
        const val INTENT_KEY_NEW_SONG = "NEW_SONG"

        const val STRING_UNKNOWN_ARTIST = "Unknown Artist"

        enum class PlayerMode {
                REPEAT,
                REPEAT_ALL,
                SHUFFLE,
                NONE
        }
}