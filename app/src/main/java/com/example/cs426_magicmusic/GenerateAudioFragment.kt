package com.example.cs426_magicmusic

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GenerateAudioFragment : Fragment() {

    private lateinit var viewModel: GenerateAudioViewModel
    private lateinit var generateButton: Button
    private lateinit var getButton: Button
    private lateinit var limitButton: Button
    private lateinit var instrumentalSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var playButton: Button
    private lateinit var newButton: Button
    private lateinit var seekBar: SeekBar
    private lateinit var inputText: EditText

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

        viewModel = ViewModelProvider(requireActivity()).get(GenerateAudioViewModel::class.java)

        inputText = view.findViewById(R.id.inputText)
        generateButton = view.findViewById(R.id.generateButton)
        newButton = view.findViewById(R.id.newButton)
        getButton = view.findViewById(R.id.getButton)
        instrumentalSwitch = view.findViewById(R.id.instrumentalSwitch)
        limitButton = view.findViewById(R.id.limitButton)
        statusText = view.findViewById(R.id.statusText)
        progressBar = view.findViewById(R.id.progressBar)
        playButton = view.findViewById(R.id.playButton)
        seekBar = view.findViewById(R.id.seekBar)

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

        generateButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("This action costs 10 credits to generate 2 songs. Continue?")
                .setPositiveButton("Let's go") { dialog, which ->
                    CoroutineScope(Dispatchers.Main).launch {
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
                CoroutineScope(Dispatchers.Main).launch {
                    requireActivity().recreate()
                }
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
                CoroutineScope(Dispatchers.Main).launch {
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
                    CoroutineScope(Dispatchers.Main).launch {
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

        instrumentalSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                is_instrumental = true
            } else {
                is_instrumental = false
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

        viewModel.statusText.observe(viewLifecycleOwner) { status ->
            statusText.text = status
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            progressBar.progress = progress
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Log.d("Toast", "change message to: $message")
            requireActivity().runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playMusic() {
        val filePath = viewModel.audioFilePath.value
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        filePath?.let {
            mediaPlayer?.apply {
                reset()
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