package com.example.cs426_magicmusic

object MusicRepository {
    fun getMusicList(query: String): List<MusicItem> {
        // query == all
        return MusicAdapter.queryAllMusic()
    }

    fun getMusic(musicId: Int): MusicItem {
        return MusicAdapter.queryMusic(musicId)
    }

//    fun getAudio(audioId: Int): AudioItem {
//        return MusicAdapter.queryAudio(audioId)
//    }
}