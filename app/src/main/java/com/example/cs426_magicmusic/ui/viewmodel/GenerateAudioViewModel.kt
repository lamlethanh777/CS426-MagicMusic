package com.example.cs426_magicmusic.ui.viewmodel

import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cs426_magicmusic.others.Constants.DEFAULT_APPLICATION_AUDIO_PATH
import com.example.cs426_magicmusic.others.Constants.DEFAULT_APPLICATION_METADATA_PATH
import com.example.cs426_magicmusic.ui.view.main.generate_audio.GenerateAudioFragment.Companion.urlArraySize
import com.example.cs426_magicmusic.ui.view.main.generate_audio.GenerateAudioFragment.Companion.urlPrefixArray
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
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom
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
    private val statusTextVisibility = MutableLiveData<Int>()
    val toastMessage = MutableLiveData<String>()
    val snackMessage = MutableLiveData<String>()
    val generateButtonVisibility = MutableLiveData<Int>()

    // Input field state

    private var totalDownloaded: Int = 0 // Number of audios completely downloaded
    private var totalGenerating: Int = 0 // Number of audios successfully posted
    private var urlArrayStartIndex: Int = 0 // Start index of urlPrefixArray

    private var progressJob: Job? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    fun increaseStartIndex(isRandom: Boolean = false) {
        val secureRandom = SecureRandom()

        urlArrayStartIndex = if (isRandom) {
            // Generate a secure random number between 1 and urlArraySize - 1
            val randomIncrement = secureRandom.nextInt(urlArraySize - 1) + 1
            (urlArrayStartIndex + randomIncrement) % urlArraySize
        } else {
            // Increase by 1 normally
            (urlArrayStartIndex + 1) % urlArraySize
        }
    }

    fun resetViewModelState() {
        progress.value = 0
        statusText.value = ""
        userInputText.value = ""

        playButtonVisibility.value = View.VISIBLE
        swapButtonVisibility.value = View.VISIBLE
        progressBarVisibility.value = View.GONE
        statusTextVisibility.value = View.VISIBLE
        generateButtonVisibility.value = View.VISIBLE
        toastMessage.value = ""
        snackMessage.value = ""

        totalDownloaded = 0
        totalGenerating = 0
        resetProgress()
    }

    init {
        resetViewModelState()
        increaseStartIndex(true)
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

    private suspend fun checkLimit(urlPrefix: String): Boolean {
        val url = "$urlPrefix/api/get_limit"
        val response = makeGetRequest(url)

        return if (response != null && response.isSuccessful) {
            val jsonResponse = response.body?.string()
            val remainToken = jsonResponse?.let { JSONObject(it).getInt("credits_left") }
            Log.d("credits_left", "$urlPrefix: $remainToken")

//            triggerToast(urlPrefix + " " + remainToken.toString());
            remainToken!! > 0  // return true if there are remaining tokens
        } else {
            Log.e("checkLimit", "Failed to get limit from $urlPrefix")
            false
        }
    }

    private fun triggerToast(message: String) {
        toastMessage.postValue(message)
    }

    private fun triggerSnack(message: String) {
        snackMessage.postValue(message)
    }

    suspend fun generateMusic(inputText: String, isInstrumental: Boolean) {
        var urlPrefix = ""
        for (i in 0 until urlArraySize) {
            // Calculate the index using the modulo operator to wrap around
            val index = (urlArrayStartIndex + i) % urlArraySize
            val url = urlPrefixArray[index]

            // Call your check function
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
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    Log.e("succeed", jsonResponse)
                }
                fetchHistoryRecords(urlPrefix)
            } else {
                triggerSnack("Current service failed. Press New task")
                generateButtonVisibility.postValue(View.GONE)
            }
        } else {
            triggerToast("No available tokens")
        }
    }

    suspend fun fetchHistoryRecords(urlPrefix: String = "") {
        var prefix = urlPrefix

        // Loop through the URL prefix array to find a valid one
        for (i in 0 until urlArraySize) {
            // Calculate the index using the modulo operator to wrap around
            val index = (urlArrayStartIndex + i) % urlArraySize
            val url = urlPrefixArray[index]

            // Call your check function
            if (checkLimit(url)) {
                prefix = url
                break
            }
        }

        // Proceed only if a valid URL prefix is found
        if (prefix.isNotEmpty()) {
            resetProgress()
            updateProgress()
            totalDownloaded = 0
            totalGenerating = 0
            val job0 = viewModelScope.async(Dispatchers.IO) {
                fetchHistoryRecordsByIndex(prefix, 0)
            }

            val job1 = viewModelScope.async(Dispatchers.IO) {
                fetchHistoryRecordsByIndex(prefix, 1)
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
        val maxAttempts = 100  // Maximum number of retries to avoid infinite looping
        var attempts = 0

        println("fetchHistoryRecordsByIndex $index")

        while (audioUrl.isNullOrEmpty() && attempts < maxAttempts) {
            val response = makeGetRequest(url)

            if (response != null && response.isSuccessful) {
                val jsonResponse = response.body?.string()

                title = extractTitleByIndex(jsonResponse, index)

                if (title == null) {
                    triggerToast("No records found")
                    resetProgress()
                    return
                }

                audioUrl = extractAudioUrlByIndex(jsonResponse, index)

                if (!audioUrl.isNullOrEmpty()) {
                    Log.e("Valid audio URL_${index}", "Valid audio URL_${index}")
                    if (title != null) {
                        downloadAudio(audioUrl, index, title)
                        downloadJsonFile(jsonResponse, index, title)
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
            triggerToast("No audio URL found.")
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

    private fun downloadJsonFile(jsonResponse: String?, index: Int = 0, title: String = "") {
        try {
            jsonResponse?.let {
                val jsonArray = JSONArray(it)

                // Check if the index is valid
                if (jsonArray.length() > index) {
                    // Extract the index-th JSON object
                    val jsonObject = jsonArray.getJSONObject(index)

                    // Convert the JSON object to a string
                    val jsonString = jsonObject.toString(4) // Pretty-print with indent

                    // Creating the directory and file
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val metadataFolder = File(downloadsDir, DEFAULT_APPLICATION_METADATA_PATH)
                    if (!metadataFolder.exists()) {
                        metadataFolder.mkdirs()  // Create the directory if it doesn't exist
                    }

                    // Generate the file name based on the title
                    val fileName = "$title - v${index + 1}.json"
                    val file = File(metadataFolder, fileName)

                    // Write JSON string to file
                    FileOutputStream(file).use { output ->
                        output.write(jsonString.toByteArray())
                        output.flush()
                    }

                    Log.d("downloadJsonFile", "File saved successfully at: ${file.absolutePath}")
                } else {
                    Log.e("downloadJsonFile", "Invalid index: $index")
                }
            }
        } catch (e: Exception) {
            Log.e("downloadJsonFile", "Failed to save JSON at index $index", e)
        }
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

    private fun downloadAudio(fileUrl: String, index: Int = 0, title: String = "") {
//        statusText.postValue("Preparing_${index} to download audio file...")

        Log.e("Prepare_${index} downloading", fileUrl)

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response =
                    client.newCall(request).execute()  // Synchronous call in background thread
                if (response.isSuccessful) {
                    val fileName = "$title - v${index + 1}.mp3"
//                    val fileName = "${title} - v${index+1} ~ loading...mp3"
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val musicFolder = File(downloadsDir, DEFAULT_APPLICATION_AUDIO_PATH)

                    if (!musicFolder.exists()) {
                        musicFolder.mkdirs()  // Create the directory if it doesn't exist
                    }
                    val file = File(musicFolder, fileName)

                    triggerToast("Downloading: $fileName")
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

                    // Update UI when download is done
                    withContext(Dispatchers.Main) {
                        totalDownloaded += 1
                        if (totalDownloaded == maxTrack) {
                            resetProgress()
                        }
                        statusText.postValue("${totalDownloaded}/${maxTrack} files downloaded successfully")
                    }
                } else {
                    // Update UI when response is unsuccessful
                    resetProgress()
                    statusText.postValue("Failed to download file: ${response.code}")
                }
            } catch (e: IOException) {
                // Handle failure to download the file
                withContext(Dispatchers.Main) {
                    resetProgress()
                    triggerSnack("Error getting device data. Please restart your device")
                }
                Log.e("GenerateAudioFragment", "File_${index} download failed", e)
            }
        }
    }

    private fun updateProgress() {
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

    private fun resetProgress() {
        progressBarVisibility.postValue(View.GONE)
        progressJob?.cancel()
        progress.value = 0
    }
}