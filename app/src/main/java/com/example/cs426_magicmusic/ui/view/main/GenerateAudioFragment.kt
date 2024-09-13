package com.example.cs426_magicmusic

import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

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
    private var exoPlayer: ExoPlayer? = null
    private var audioFileIndex: Int = 0
    private var is_instrumental: Boolean = false

    companion object {
        @JvmStatic
        fun newInstance() = GenerateAudioFragment().apply {}

        val urlPrefixArray = arrayOf(
            "https://magic-music-acc-2.vercel.app",
            "https://magic-music-acc-3.vercel.app",
            "https://magic-music-acc-4.vercel.app",
            "https://magic-music-acc-5.vercel.app",
            "https://magic-music-acc-0.vercel.app",
            "https://magic-music-acc-1.vercel.app"
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
                viewModel.userInputText.value = s.toString()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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
                        dialog.dismiss()
                    }
                    .show()
            } else {
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
                    dialog.dismiss()
                }
                .show()
        }

        getButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Download 2 recent tracks")
                .setPositiveButton("Yes") { dialog, which ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.fetchHistoryRecords()
                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        limitButton.setOnClickListener {
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
            is_instrumental = isChecked
        }

        lyricSwitch.setOnCheckedChangeListener { _, isChecked ->
            lyricTextView.visibility = if (isChecked) View.VISIBLE else View.GONE
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

        exoPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            exoPlayer = null
            playButton.setBackgroundResource(R.drawable.paused_button)
        }

        playMusic()
    }

    private fun playMusic() {
        playSongJob = lifecycleScope.launch(Dispatchers.Main) {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(requireContext()).build()
            }
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "magicmusic/audio")
            val files = downloadsDir.listFiles()

            if (files != null && files.isNotEmpty()) {
                loadLyric()
                val sizeDir = files.size
                val filePath = files.getOrNull(sizeDir - audioFileIndex - 1)?.toString()
                viewModel.statusText.postValue("${File(filePath).name}")

                filePath?.let {
                    exoPlayer?.apply {
                        setMediaItem(MediaItem.fromUri(it))
                        prepare()
                        play()
                    }

                    playButton.setBackgroundResource(R.drawable.played_button)

                    playButton.setOnClickListener {
                        exoPlayer?.let { player ->
                            if (player.isPlaying) {
                                player.pause()
                                playButton.setBackgroundResource(R.drawable.paused_button)
                            } else {
                                player.play()
                                playButton.setBackgroundResource(R.drawable.played_button)
                            }
                        }
                    }

                    exoPlayer?.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_ENDED) {
                                playButton.setBackgroundResource(R.drawable.paused_button)
                            }

                            if (state == Player.STATE_READY && exoPlayer?.playWhenReady == true) {
                                seekBar.max = exoPlayer?.duration?.toInt() ?: 0
                                seekBar.visibility = View.VISIBLE

                                activity?.runOnUiThread(object : Runnable {
                                    override fun run() {
                                        if (exoPlayer == null) {
                                            return
                                        }
                                        if (exoPlayer!!.isPlaying || exoPlayer!!.playbackState == Player.STATE_READY) {
                                            seekBar.progress = exoPlayer?.currentPosition?.toInt() ?: 0
                                        }
                                        if (exoPlayer!!.isPlaying) {
                                            seekBar.postDelayed(this, 100)
                                        }
                                    }
                                })
                            }
                        }
                    })

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                exoPlayer?.seekTo(progress.toLong())
                            }
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    })
                }
            } else {
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
            filePath = files.getOrNull(sizeDir - 1).toString()
        }
        val file = File(filePath)
        if (file.exists()) {
            val lyrics = file.readText()
            lyricTextView.text = lyrics
        } else {
            lyricTextView.text = "Lyrics file not found!"
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.release()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}