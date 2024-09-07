package com.example.cs426_magicmusic

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.IpPrefix
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.cs426_magicmusic.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import org.json.JSONArray
import org.json.JSONObject

class GenerateAudioFragment : Fragment() {

    private lateinit var generateButton: Button
    private lateinit var getButton: Button
    private lateinit var limitButton: Button
    private lateinit var instrumentalSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var playButton: Button
    private lateinit var seekBar: SeekBar
    private lateinit var inputText: EditText
    private lateinit var urlPrefix: String

    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String? = null
    private var is_instrumental: Boolean = false

    companion object {
        @JvmStatic
        fun newInstance() = GenerateAudioFragment().apply {}

        val urlPrefixArray = arrayOf(
            "https://magic-music-acc-5.vercel.app",
            "https://magic-music-acc-4.vercel.app",
            "https://magic-music-acc-3.vercel.app",
            "https://magic-music-acc-2.vercel.app",
            "https://magic-music-acc-1.vercel.app",
            "https://magic-music-acc-0.vercel.app"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_generate_audio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputText = view.findViewById(R.id.inputText)
        generateButton = view.findViewById(R.id.generateButton)
        getButton = view.findViewById(R.id.getButton)
        instrumentalSwitch = view.findViewById(R.id.instrumentalSwitch)
        limitButton = view.findViewById(R.id.limitButton)
        statusText = view.findViewById(R.id.statusText)
        progressBar = view.findViewById(R.id.progressBar)
        playButton = view.findViewById(R.id.playButton)
        seekBar = view.findViewById(R.id.seekBar)


        generateButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmation")
                    .setMessage("This action costs 10 credits to generate 2 songs. Continue?")
                    .setPositiveButton("Let's go") { dialog, which ->
                        CoroutineScope(Dispatchers.Main).launch {
                            generateMusic(text)
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // If the user cancels, just dismiss the dialog
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        getButton.setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch {
                fetchHistoryRecords()
            }
        }

        limitButton.setOnClickListener{
            for (url in urlPrefixArray) {
                CoroutineScope(Dispatchers.Main).launch {
                    checkLimit(url)
                }
            }
        }

        playButton.setOnClickListener {
            playMusic()
        }

        instrumentalSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                is_instrumental = true
            } else {
                is_instrumental = false
            }
        }
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

            showToast(urlPrefix + " " + remainToken.toString());
            remainToken!! > 0  // return true if there are remaining tokens
        } else {
            Log.e("checkLimit", "Failed to get limit from $urlPrefix")
            false
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    private suspend fun generateMusic(inputText: String) {
        showToast("Start generating")
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
            "make_instrumental": $is_instrumental,
            "wait_audio": false
        }
        """.trimIndent()

            val url = "$urlPrefix/api/generate"
            val response = makePostRequest(url, jsonPayload)

            if (response != null && response.isSuccessful) {
                showToast("POST succeed")
                Log.e("succeed", response.toString())
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    Log.e("succeed", jsonResponse)
                }
                fetchHistoryRecords(urlPrefix)
            } else {
                showToast("Failed to generate music")
            }
        } else {
            showToast("No available tokens")
        }
    }

    private suspend fun fetchHistoryRecords(_urlPrefix: String = "") {
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
            val job0 = CoroutineScope(Dispatchers.IO).async {
                fetchHistoryRecordsByIndex(urlPrefix, 0)
            }

            val job1 = CoroutineScope(Dispatchers.IO).async {
                fetchHistoryRecordsByIndex(urlPrefix, 1)
            }
        } else {
            showToast("No available tokens")
        }
    }

    private suspend fun fetchHistoryRecordsByIndex(urlPrefix: String = "", index: Int = 0) {
        val url = "$urlPrefix/api/get"
        showToast("fetch_{$index}")
        var audioUrl: String? = null
        var lyricUrl: String? = null
        var maxAttempts = 5  // Maximum number of retries to avoid infinite looping
        var attempts = 0

        while (audioUrl.isNullOrEmpty() && attempts < maxAttempts) {
            val response = makeGetRequest(url)

            if (response != null && response.isSuccessful) {
                val jsonResponse = response.body?.string()

                audioUrl = extractAudioUrlByIndex(jsonResponse, index)
                lyricUrl = extractLyricUrlByIndex(jsonResponse, index)

                if (!audioUrl.isNullOrEmpty()) {
                    Log.e("Valid audio URL_${index}", audioUrl)
                    downloadAudio(audioUrl, index)  // Proceed to download
                    downloadLyric(lyricUrl, index)
                    break
                } else {
                    Log.e("No audio URL_${index}", "Retrying...")
                    showToast("No audio URL_${index} found, retrying... ($attempts)")
                }
            } else {
                Log.e("Fetch Error", "Failed to fetch history_${index}, retrying... ($attempts)")
                showToast("Failed to fetch history_${index}, retrying... ($attempts)")
            }

            attempts++
            delay(1000)  // Delay for 1 second before retrying
        }

        if (audioUrl.isNullOrEmpty()) {
            showToast("Max retries reached. No audio URL found.")
        }
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

    private fun downloadLyric(fileUrl: String?, index: Int = 0) {
        // Replacing '\n' in the lyrics string with actual new line characters
        val formattedLyric = fileUrl
            ?.replace("\\n", System.lineSeparator())  // Replace \n with a single new line
            ?.replace("[", System.lineSeparator() + "[")  // Replace \n[ with double new line and [



        // Creating the file name using the index
        val fileName = "output_tmp_${index}_lyric.txt"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            // Writing the formatted lyrics into the file
            if (formattedLyric != null) {
                file.writeText(formattedLyric)
            }
            // Optional: You can provide feedback that the file was saved successfully
            println("File saved successfully at: ${file.absolutePath}")
        } catch (e: IOException) {
            // Handle any errors during file writing
            e.printStackTrace()
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

    private fun downloadAudio(fileUrl: String, index: Int = 0) {
        // Show downloading UI
        Log.e("Called downloading_${index}", fileUrl)

        CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = View.VISIBLE
            statusText.text = "Preparing_${index} to download audio file..."
            statusText.visibility = View.VISIBLE
            Log.e("Prepare_${index} downloading", fileUrl)
        }

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()  // Synchronous call in background thread
                if (response.isSuccessful) {
                    val fileName = "output_tmp_${index}.mp4"
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                    // Write the file
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    audioFilePath = file.absolutePath

                    // Update UI when download is done
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        statusText.text = "File_${index} downloaded successfully: ${file.absolutePath}"
                        statusText.visibility = View.VISIBLE
                        playButton.visibility = View.VISIBLE
                    }
                } else {
                    // Update UI when response is unsuccessful
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        statusText.text = "Failed to download file_${index}: ${response.code}"
                        statusText.visibility = View.VISIBLE
                    }
                }
            } catch (e: IOException) {
                // Handle failure to download the file
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    statusText.text = "Failed to download file_${index}"
                    statusText.visibility = View.VISIBLE
                }
                Log.e("GenerateAudioFragment", "File_${index} download failed", e)
            }
        }
    }

    private fun playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        audioFilePath?.let {
            mediaPlayer?.apply {
                reset() // Reset the MediaPlayer to its uninitialized state
                setDataSource(it)
                prepare()
                start()
            }

            playButton.text = "Pause Music"
            playButton.setOnClickListener {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        playButton.text = "Play Music"
                    } else {
                        player.start()
                        playButton.text = "Pause Music"
                    }
                }
            }

            mediaPlayer?.setOnCompletionListener {
                playButton.text = "Play Music"
            }

            mediaPlayer?.setOnPreparedListener {
                seekBar.max = mediaPlayer?.duration ?: 0
                seekBar.visibility = View.VISIBLE

                activity?.runOnUiThread(object : Runnable {
                    override fun run() {
                        seekBar.progress = mediaPlayer?.currentPosition ?: 0
                        if (mediaPlayer?.isPlaying == true) {
                            seekBar.postDelayed(this, 100)
                        }
                    }
                })
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        } ?: run {
            statusText.text = "No audio file to play"
            statusText.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.release() // Release MediaPlayer resources
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release() // Release MediaPlayer resources
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Release the MediaPlayer resources if initialized
        mediaPlayer = null // Set it to null to avoid further usage
    }
}