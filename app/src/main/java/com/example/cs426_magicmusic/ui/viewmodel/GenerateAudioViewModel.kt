package com.example.cs426_magicmusic

import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs426_magicmusic.GenerateAudioFragment.Companion.urlPrefixArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class GenerateAudioViewModel : ViewModel() {
    private val maxTrack = 2

    val progress = MutableLiveData<Int>()
    val statusText = MutableLiveData<String>()
    val userInputText = MutableLiveData<String>()

    // Visibility states
    val playButtonVisibility = MutableLiveData<Int>()
    val swapButtonVisibility = MutableLiveData<Int>()
    val progressBarVisibility = MutableLiveData<Int>()
    val statusTextVisibility = MutableLiveData<Int>()
    val toastMessage = MutableLiveData<String>()

    // Input field state

    var totalDownloaded: Int = 0 // Number of audios completely downloaded
    var totalGenerating: Int = 0 // Number of audios successfully posted
    private var progressJob: Job? = null

    private val REQUEST_WRITE_PERMISSION_CODE = 1001


    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    fun resetViewModelState() {
        progress.value = 0
        statusText.value = ""
        userInputText.value = ""

        playButtonVisibility.value = View.VISIBLE
        swapButtonVisibility.value = View.VISIBLE
        progressBarVisibility.value = View.GONE
        statusTextVisibility.value = View.VISIBLE
        toastMessage.value = ""

        totalDownloaded = 0
        totalGenerating = 0
        resetProgress()
    }

    init {
        resetViewModelState()
    }

    // Function for POST requests
    private suspend fun makePostRequest(url: String, jsonPayload: String): Response? {
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = jsonPayload.toRequestBody(mediaType)


        Log.e("jsonPayLoad0", jsonPayload)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute()
            } catch (e: IOException) {
                Log.e("API Error", "POST request failed", e)
                null
            }
        }
    }

    // Function for GET requests
    private suspend fun makeGetRequest(url: String): Response? {
        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute()
            } catch (e: IOException) {
                Log.e("API Error", "GET request failed", e)
                null
            }
        }
    }

    suspend fun checkLimit(urlPrefix: String): Boolean {
        val url = "$urlPrefix/api/get_limit"
        val response = makeGetRequest(url)

        return if (response != null && response.isSuccessful) {
            val jsonResponse = response.body?.string()
            val remainToken = jsonResponse?.let { JSONObject(it).getInt("credits_left") }
            Log.d("credits_left", "$urlPrefix: $remainToken")

            triggerToast(urlPrefix + " " + remainToken.toString());
            remainToken!! > 0  // return true if there are remaining tokens
        } else {
            Log.e("checkLimit", "Failed to get limit from $urlPrefix")
            false
        }
    }

    private fun triggerToast(message: String) {
        toastMessage.postValue(message)
    }

    suspend fun generateMusic(inputText: String, isInstrumental: Boolean) {
        triggerToast("Start generating")
        Log.e("Generate", "Start generating")

        var urlPrefix = ""
        for (url in urlPrefixArray) {
            if (checkLimit(url)) {
                urlPrefix = url
                break
            }
        }

        if (urlPrefix.isNotEmpty()) {
            val jsonPayload = """
            {
                "prompt": "$inputText",
                "make_instrumental": $isInstrumental,
                "wait_audio": false
            }
            """.trimIndent()

            val url = "$urlPrefix/api/generate"
            val response = makePostRequest(url, jsonPayload)

            if (response != null && response.isSuccessful) {
                triggerToast("POST succeed")
                Log.e("succeed", response.toString())
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    Log.e("succeed", jsonResponse)
                }
                fetchHistoryRecords(urlPrefix)
            } else {
                triggerToast("Failed to generate music")
            }
        } else {
            triggerToast("No available tokens")
        }
    }

    suspend fun fetchHistoryRecords(_urlPrefix: String = "") {
        println("fetchHistoryRecords")
        var urlPrefix = _urlPrefix

        // Loop through the URL prefix array to find a valid one
        if (urlPrefix == "") {
            for (url in urlPrefixArray) {
                if (checkLimit(url)) {
                    urlPrefix = url
                    break
                }
            }
        }

        // Proceed only if a valid URL prefix is found
        if (urlPrefix.isNotEmpty()) {
            resetProgress()
            updateProgress()
            totalDownloaded = 0
            totalGenerating = 0
            val job0 = viewModelScope.async(Dispatchers.IO) {
                fetchHistoryRecordsByIndex(urlPrefix, 0)
            }

            val job1 = viewModelScope.async(Dispatchers.IO) {
                fetchHistoryRecordsByIndex(urlPrefix, 1)
            }
        } else {
            println("No available tokens")
            triggerToast("No available tokens")
        }
    }

    private suspend fun fetchHistoryRecordsByIndex(urlPrefix: String = "", index: Int = 0) {
        val url = "$urlPrefix/api/get"
        var audioUrl: String? = null
        var lyricUrl: String?
        var title: String?
        var maxAttempts = 100  // Maximum number of retries to avoid infinite looping
        var attempts = 0

        println("fetchHistoryRecordsByIndex $index")

        while (audioUrl.isNullOrEmpty() && attempts < maxAttempts) {
            val response = makeGetRequest(url)

            if (response != null && response.isSuccessful) {
                val jsonResponse = response.body?.string()

                audioUrl = extractAudioUrlByIndex(jsonResponse, index)
                lyricUrl = extractLyricUrlByIndex(jsonResponse, index)
                title = extractTitleByIndex(jsonResponse, index)

                if (!audioUrl.isNullOrEmpty()) {
                    Log.e("Valid audio URL_${index}", "Valid audio URL_${index}")
                    if (title != null) {
                        downloadAudio(audioUrl, index, title)
                        downloadLyric(lyricUrl, index, title)
                    }  // Proceed to download
                    break
                } else {
                    Log.e("No audio URL_${index}", "Retrying...")
                }
            } else {
                Log.e("Fetch Error", "Failed to fetch history_${index}, retrying... ($attempts)")
            }

            attempts++
            delay(2000)  // Delay for 1 second before retrying
        }

        if (audioUrl.isNullOrEmpty()) {
            triggerToast("Max retries reached_${index}. No audio URL found.")
        }
    }

    private fun extractTitleByIndex(jsonResponse: String?, index: Int = 0): String? {
        Log.e("extractAudioUrl", "extract index_${index}")
        try {
            jsonResponse?.let {
                val jsonArray = JSONArray(it)
                // Get the last item in the array (most recent record)
                if (jsonArray.length() > index) {
                    val title = jsonArray.getJSONObject(index).getString("title")
                    return title
                }
            }
        } catch (e: Exception) {
            Log.e("GenerateTitleFragment", "Failed_${index} to parse JSON", e)
        }
        return null
    }

    private fun extractLyricUrlByIndex(jsonResponse: String?, index: Int = 0): String? {
        Log.e("extractAudioUrl", "extract index_${index}")
        try {
            jsonResponse?.let {
                val jsonArray = JSONArray(it)
                // Get the last item in the array (most recent record)
                if (jsonArray.length() > index) {
                    val lyricUrl = jsonArray.getJSONObject(index).getString("lyric")
                    return lyricUrl
                }
            }
        } catch (e: Exception) {
            Log.e("GenerateLyricFragment", "Failed_${index} to parse JSON", e)
        }
        return null
    }

    private fun extractAudioUrlByIndex(jsonResponse: String?, index: Int = 0): String? {
        Log.e("extractAudioUrl", "extract index_${index}")
        try {
            jsonResponse?.let {
                val jsonArray = JSONArray(it)
                // Get the last item in the array (most recent record)
                if (jsonArray.length() > index) {
                    val audioUrl = jsonArray.getJSONObject(index).getString("audio_url")
                    return audioUrl
                }
            }
        } catch (e: Exception) {
            Log.e("GenerateAudioFragment", "Failed_${index} to parse JSON", e)
        }
        return null
    }


    private fun downloadLyric(fileUrl: String?, index: Int = 0, title: String = "") {
        // Replacing '\n' in the lyrics string with actual new line characters
        val formattedLyric = fileUrl
            ?.replace("\\n", System.lineSeparator())  // Replace \n with a single new line
            ?.replace("[", System.lineSeparator() + "[")  // Replace \n[ with double new line and [


        // Creating the file name using the index
        val fileName = "${title} - v${index + 1}.txt"
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val musicFolder = File(downloadsDir, "magicmusic/lyric")
        if (!musicFolder.exists()) {
            musicFolder.mkdirs()  // Create the directory if it doesn't exist
        }
        val file = File(musicFolder, fileName)

        try {
            // Writing the formatted lyrics into the file
            if (formattedLyric != null) {
                val contentToWrite = "$title${System.lineSeparator()}${formattedLyric ?: ""}"
                file.writeText(contentToWrite)
            }
            // Optional: You can provide feedback that the file was saved successfully
            println("File saved successfully at: ${file.absolutePath}")
        } catch (e: IOException) {
            // Handle any errors during file writing
            e.printStackTrace()
        }
    }

    private fun downloadAudio(fileUrl: String, index: Int = 0, title: String = "") {
        statusText.postValue("Preparing_${index} to download audio file...")

        Log.e("Prepare_${index} downloading", fileUrl)

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    client.newCall(request).execute()  // Synchronous call in background thread
                if (response.isSuccessful) {
                    val fileName = "${title} - v${index + 1}.mp3"
//                    val fileName = "${title} - v${index+1} ~ loading...mp3"
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val musicFolder = File(downloadsDir, "magicmusic/audio")

                    if (!musicFolder.exists()) {
                        musicFolder.mkdirs()  // Create the directory if it doesn't exist
                    }
                    val file = File(musicFolder, fileName)

                    triggerToast("Generating: $fileName")
                    totalGenerating += 1
                    if (totalGenerating == maxTrack) {
                        playButtonVisibility.postValue(View.VISIBLE)
                        swapButtonVisibility.postValue(View.VISIBLE)
                    }

                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    triggerToast("Completed: $fileName")

                    val renamedFile = File(musicFolder, "${title} - v${index + 1}.mp3")
                    file.renameTo(renamedFile)

                    // Update UI when download is done
                    withContext(Dispatchers.Main) {
                        totalDownloaded += 1
                        if (totalDownloaded == maxTrack) {
                            resetProgress()
                        }
                        statusText.postValue("${totalDownloaded}/${maxTrack.toString()} files downloaded successfully: ${renamedFile.absolutePath}")
                    }
                } else {
                    // Update UI when response is unsuccessful
                    resetProgress()
                    statusText.postValue("Failed to download file_${index}: ${response.code}")
                }
            } catch (e: IOException) {
                // Handle failure to download the file
                withContext(Dispatchers.Main) {
                    resetProgress()
                    statusText.postValue("Failed to download file_${index}")
                }
                Log.e("GenerateAudioFragment", "File_${index} download failed", e)
            }
        }
    }

    fun updateProgress() {
        progressJob = viewModelScope.launch(Dispatchers.Main) {
            progressBarVisibility.postValue(View.VISIBLE)
            while (progress.value!! < 100) {
                val i = progress.value
                if (i != null) {
                    progress.postValue(i + 1)
                }
                delay(1500)
            }
            progressBarVisibility.postValue(View.GONE)
            progress.value = 0
        }
    }

    fun resetProgress() {
        progressBarVisibility.postValue(View.GONE)
        progressJob?.cancel()
        progress.value = 0
    }
}