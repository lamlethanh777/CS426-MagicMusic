package com.example.cs426_magicmusic

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class GenerateAudioFragment : Fragment() {

    private lateinit var viewModel: GenerateAudioViewModel
    private lateinit var generateButton: Button
    private lateinit var getButton: Button
    private lateinit var limitButton: Button
    private lateinit var instrumentalSwitch: Switch
    private lateinit var lyricSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var newButton: Button
    private lateinit var playButton: ImageButton
    private lateinit var swapButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var inputText: EditText
    private lateinit var lyricTextView: TextView

    private var playSongJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFileIndex: Int = 0
    private var is_instrumental: Boolean = false

    companion object {
        @JvmStatic
        fun newInstance() = GenerateAudioFragment().apply {}

        val urlPrefixArray = arrayOf(
            "https://magic-music-acc-1.vercel.app",
            "https://magic-music-acc-2.vercel.app",
            "https://magic-music-acc-3.vercel.app",
            "https://magic-music-acc-4.vercel.app",
            "https://magic-music-acc-5.vercel.app",
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

        viewModel = ViewModelProvider(requireActivity()).get(GenerateAudioViewModel::class.java)


        inputText = view.findViewById(R.id.inputText)
        generateButton = view.findViewById(R.id.generateButton)
        newButton = view.findViewById(R.id.newButton)
        getButton = view.findViewById(R.id.getButton)
        instrumentalSwitch = view.findViewById(R.id.instrumentalSwitch)
        lyricSwitch = view.findViewById(R.id.lyricSwitch)
        limitButton = view.findViewById(R.id.limitButton)
        statusText = view.findViewById(R.id.statusText)
        progressBar = view.findViewById(R.id.progressBar)
        playButton = view.findViewById(R.id.playButton)
        swapButton = view.findViewById(R.id.swapButton)
        seekBar = view.findViewById(R.id.seekBar)
        lyricTextView = view.findViewById(R.id.lyricTextView)


        println("userin ${viewModel.userInputText.value}")
        inputText.setText(viewModel.userInputText.value)


        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Update ViewModel when the text changes
                viewModel.userInputText.value = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // You can leave this empty if you don't need it
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // You can leave this empty if you don't need it
            }
        })

        setupObservers()

        println("created")

        generateButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmation")
                    .setMessage("This action costs 10 credits to generate 2 songs. Continue?")
                    .setPositiveButton("Let's go") { dialog, which ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            viewModel.generateMusic(text, is_instrumental)
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        // If the user cancels, just dismiss the dialog
                        dialog.dismiss()
                    }
                    .show()
            }
            else {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Give me some words!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        newButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("The UI will be refreshed, but the previous task is still being processed?")
                .setPositiveButton("New") { dialog, which ->
                    viewModel.resetViewModelState()
                    val fragmentTransaction = parentFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.main_fragment, GenerateAudioFragment())
                    fragmentTransaction.commit()
                }
                .setNegativeButton("Wait") { dialog, which ->
                    // If the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        getButton.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Download 2 recent tracks")
                .setPositiveButton("Yes") { dialog, which ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.fetchHistoryRecords()
                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    // If the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        limitButton.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Check remain credits of all accounts. No credit loss")
                .setPositiveButton("Yes") { dialog, which ->
                    for (url in urlPrefixArray) {
                        lifecycleScope.launch(Dispatchers.Main) {
                            viewModel.checkLimit(url)
                        }
                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    // If the user cancels, just dismiss the dialog
                    dialog.dismiss()
                }
                .show()
        }

        playButton.setOnClickListener {
            playMusic()
        }

        swapButton.setOnClickListener {
            swapSong()
        }


        instrumentalSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                is_instrumental = true
            } else {
                is_instrumental = false
            }
        }

        lyricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lyricTextView.visibility = View.VISIBLE
            } else {
                lyricTextView.visibility = View.GONE
            }
        }
    }

    private fun setupObservers() {
        viewModel.progressBarVisibility.observe(viewLifecycleOwner) { visibility ->
            progressBar.visibility = visibility
        }

        viewModel.playButtonVisibility.observe(viewLifecycleOwner) { visibility ->
            playButton.visibility = visibility
        }

        viewModel.swapButtonVisibility.observe(viewLifecycleOwner) { visibility ->
            swapButton.visibility = visibility
        }

        viewModel.statusText.observe(viewLifecycleOwner) { status ->
            statusText.text = status
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            progressBar.progress = progress
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty())
                requireActivity().runOnUiThread {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun swapSong() {
        audioFileIndex = 1 - audioFileIndex
        playSongJob?.cancel()

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
            playButton.setBackgroundResource(R.drawable.paused_button)
        }

        playMusic()
    }

    private fun playMusic() {
        playSongJob = lifecycleScope.launch(Dispatchers.Main) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "magicmusic/audio")
            val files = downloadsDir.listFiles()

            // Check if the files array is null or empty
            if (files != null && files.isNotEmpty()) {
                loadLyric()
                val sizeDir = files.size
                val filePath = files.getOrNull(sizeDir - audioFileIndex - 1)?.toString()  // Get the file safely
                viewModel.statusText.postValue("${File(filePath).name}")

                filePath?.let {
                    mediaPlayer?.apply {
                        reset()
                        setDataSource(it)
                        prepare()
                        start()
                    }

                    playButton.setBackgroundResource(R.drawable.played_button)

                    playButton.setOnClickListener {
                        mediaPlayer?.let { player ->
                            if (player.isPlaying) {
                                player.pause()
                                playButton.setBackgroundResource(R.drawable.paused_button)
                            } else {
                                player.start()
                                playButton.setBackgroundResource(R.drawable.played_button)
                            }
                        }
                    }

                    mediaPlayer?.setOnCompletionListener {
                        playButton.setBackgroundResource(R.drawable.paused_button)
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
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                mediaPlayer?.seekTo(progress)
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    })
                }
            } else {
                // Handle case where files array is null or empty
                viewModel.statusText.postValue("No audio files found in the directory")
            }
        }
    }

    private fun loadLyric() {
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "magicmusic/lyric")
        val files = downloadsDir.listFiles()
        var filePath: String? = ""
        var sizeDir: Int = files.size
        if (files != null && files.isNotEmpty()) {
            filePath = files.getOrNull(sizeDir - 1).toString()  // Get the first file
        }
        val file = File(filePath)
        if (file.exists()) {
            val inputStream = FileInputStream(file)
            val reader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val lyrics = reader.readText()
            reader.close()

            // Set the lyrics text to the TextView
            lyricTextView.text = lyrics
        } else {
            lyricTextView.text = "Lyrics file not found!"
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