package com.example.cs426_magicmusic.data.source.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cs426_magicmusic.data.entity.Album
import com.example.cs426_magicmusic.data.entity.relation.albums_songs.AlbumSong
import com.example.cs426_magicmusic.data.entity.Artist
import com.example.cs426_magicmusic.data.entity.relation.artists_songs.ArtistSong
import com.example.cs426_magicmusic.data.entity.Playlist
import com.example.cs426_magicmusic.data.entity.relation.playlists_songs.PlaylistSong
import com.example.cs426_magicmusic.data.entity.Song
import com.example.cs426_magicmusic.data.source.db.dao.AlbumDao
import com.example.cs426_magicmusic.data.source.db.dao.AlbumSongDao
import com.example.cs426_magicmusic.data.source.db.dao.ArtistDao
import com.example.cs426_magicmusic.data.source.db.dao.ArtistSongDao
import com.example.cs426_magicmusic.data.source.db.dao.PlaylistDao
import com.example.cs426_magicmusic.data.source.db.dao.PlaylistSongDao
import com.example.cs426_magicmusic.data.source.db.dao.SongDao
import com.example.cs426_magicmusic.others.Constants.RUNNING_DATABASE_NAME

@Database(
    entities = [Album::class,
                Playlist::class,
                Artist::class,
                Song::class,
                AlbumSong::class,
                PlaylistSong::class,
                ArtistSong::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun artistDao(): ArtistDao
    abstract fun songDao(): SongDao
    abstract fun albumSongDao(): AlbumSongDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun artistSongDao(): ArtistSongDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    RUNNING_DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
