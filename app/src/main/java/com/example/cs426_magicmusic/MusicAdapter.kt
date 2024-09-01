package com.example.cs426_magicmusic

import android.annotation.SuppressLint
import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@SuppressLint("StaticFieldLeak")
object MusicAdapter {

    private lateinit var audio:    MutableList<Map<String, String>>
    private lateinit var author:   MutableList<Map<String, String>>
    private lateinit var music:    MutableList<Map<String, String>>
    private lateinit var relation: MutableList<Map<String, String>>

    private lateinit var context: Context
    fun init(context: Context) {
        this.context = context

        audio = readTsv(R.raw.audio_table)
        author = readTsv(R.raw.author_table)
        music = readTsv(R.raw.music_table)
        relation = readTsv(R.raw.author_music_table)
    }

//    fun insert() {}
//    fun delete() {}
//    fun modify() {}
//    fun query() {}

    fun queryAllMusic(): List<MusicItem> {
        val resultList: MutableList<MusicItem> = ArrayList()
        music.forEach { musicRow ->
            val authorList: MutableList<String> = ArrayList()
            relation.forEach { relationRow ->
                if (relationRow["music_id"] == musicRow["id"]) {
                    author.forEach { authorRow ->
                        if (authorRow["id"] == relationRow["author_id"]) {
                            authorList.add(authorRow["author_name"]!!)
                        }
                    }
                }
            }
            val resourceNameParts = musicRow["icon_resource"]!!.split(".")
            val resourceType = resourceNameParts[0]
            val resourceNameOnly = resourceNameParts[1]

            resultList.add(MusicItem(
                musicRow["title"]!!,
                authorList,
                context.resources.getIdentifier(resourceNameOnly, resourceType, context.packageName),
                musicRow["audio_id"]!!.toInt(),
                musicRow["id"]!!.toInt()))
        }
        return resultList
    }
    fun queryMusic(musicId: Int): MusicItem {
        music.forEach { musicRow ->
            if (musicRow["id"]!!.toInt() == musicId) {
                val authorList: MutableList<String> = ArrayList()
                relation.forEach { relationRow ->
                    if (relationRow["music_id"] == musicRow["id"]) {
                        author.forEach { authorRow ->
                            if (authorRow["id"] == relationRow["author_id"]) {
                                authorList.add(authorRow["author_name"]!!)
                            }
                        }
                    }
                }
                val resourceNameParts = musicRow["icon_resource"]!!.split(".")
                val resourceType = resourceNameParts[0]
                val resourceNameOnly = resourceNameParts[1]

                return MusicItem(
                    musicRow["title"]!!,
                    authorList,
                    context.resources.getIdentifier(
                        resourceNameOnly,
                        resourceType,
                        context.packageName
                    ),
                    musicRow["audio_id"]!!.toInt(),
                    musicRow["id"]!!.toInt()
                )
            }
        }
        return MusicItem("NA", listOf("NA"), R.drawable.home_black_25_24, -1, -1)
    }

    /**
     * return resourceId of the given audioId
     */
    fun queryAudio(audioId: Int): Int {
        audio.forEach { audioRow ->
            if (audioRow["id"]!!.toInt() == audioId) {
                val resourceNameParts = audioRow["audio_resource"]!!.split(".")
                val resourceType = resourceNameParts[0]
                val resourceNameOnly = resourceNameParts[1]
                return context.resources.getIdentifier(resourceNameOnly, resourceType, context.packageName)
            }
        }
        return -1
    }

    private fun readTsv(resId: Int): MutableList<Map<String, String>> {
        val inputStream: InputStream = this.context.resources.openRawResource(resId)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        val resultList: MutableList<Map<String, String>> = ArrayList()

        val headers = bufferedReader.readLine()?.split("\t") ?: return resultList
        bufferedReader.useLines { lines ->
            lines.forEach { line ->
                val values = line.split("\t").toTypedArray()
                val map = mutableMapOf<String, String>()
                headers.forEachIndexed { index, header ->
                    map[header] = values.getOrElse(index) { "" }
                }
                resultList.add(map)
            }
        }
        return resultList
    }
}