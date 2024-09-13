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

        const val NEXT_SONG = 1
        const val PREVIOUS_SONG = -1
        const val CURRENT_SONG = 0

        const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SKIP_NEXT = "ACTION_SKIP_NEXT"
        const val ACTION_SKIP_PREVIOUS = "ACTION_SKIP_PREVIOUS"
        const val ACTION_STOP = "ACTION_STOP"

        const val PLAYER_CHANNEL_ID = "music_channel"
        const val PLAYER_CHANNEL_NAME = "Music Channel"
        const val PLAYER_CHANNEL_DESCRIPTION = "This channel is used for music notifications"
        const val PLAYER_NOTIFICATION_REQUEST_CODE = 0
        const val PLAYER_NOTIFICATION_ID = 1
        const val PLAYER_NOTIFICATION_CONTENT_TEXT = "Currently playing"
        const val PLAYER_NOTIFICATION_CONTENT_TITLE = "MagicMusic"

        /**
         * Intent keys when sending data between activities
         */
        const val INTENT_KEY_NEW_SONG_LIST = "NEW_SONG_LIST"
        const val INTENT_KEY_SONG_INDEX = "SONG_INDEX"

        const val STRING_UNKNOWN_ARTIST = "Unknown Artist"
        const val STRING_UNKNOWN_ALBUM = "Unknown Album"
        const val STRING_UNKNOWN_TITLE = "Unknown Title"
        const val STRING_SYSTEM_UNKNOWN_TAG = "<unknown>"

        const val NUMBER_OF_REPEAT_MODE = 3
        const val PLAYER_REPEAT_MODE_NONE = 0
        const val PLAYER_REPEAT_MODE_ONE = 1
        const val PLAYER_REPEAT_MODE_ALL = 2

        const val NUMBER_OF_SHUFFLE_MODE = 2
        const val PLAYER_SHUFFLE_MODE_OFF = false
        const val PLAYER_SHUFFLE_MODE_ON = true

        const val NUMBER_OF_PLAY_DIRECTION = 2
        const val PLAY_DIRECTION_NEXT = 1
        const val PLAY_DIRECTION_PREVIOUS = -1
        const val PLAY_DIRECTION_NONE = 0

        const val PLAYER_DELAY_BETWEEN_SONGS = 1000L
        const val STRING_UNKNOWN_LYRIC = "No lyric available"
}