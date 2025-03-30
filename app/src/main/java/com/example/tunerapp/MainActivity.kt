package com.example.tunerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tunerapp.tuner.AudioProcessor
import com.example.tunerapp.tuner.Note
import com.example.tunerapp.tuner.PermissionManager
import com.example.tunerapp.tuner.PermissionResultCallback
import com.example.tunerapp.tuner.PitchListener
import com.example.tunerapp.utilities.TunerLineManager
import kotlinx.coroutines.Job


class MainActivity : AppCompatActivity(), PitchListener, PermissionResultCallback {

    private lateinit var noteTextView: TextView
    private lateinit var centerOffTextView: TextView
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var permissionManager: PermissionManager
    private lateinit var tunerLineManager: TunerLineManager

    private var updateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        noteTextView = findViewById(R.id.activity_main_note_textView)
        centerOffTextView = findViewById(R.id.cents_off_textView)


        audioProcessor = AudioProcessor(this)
        permissionManager = PermissionManager(this)
        permissionManager.register(this)
        tunerLineManager = TunerLineManager(this)

        setupTunerLines()
    }

    private fun setupTunerLines() {
        val lineIds = listOf(
            R.id.tuner_line_minus_50 to -50,
            R.id.tuner_line_minus_40 to -40,
            R.id.tuner_line_minus_30 to -30,
            R.id.tuner_line_minus_20 to -20,
            R.id.tuner_line_minus_10 to -10,
            R.id.tuner_line_0 to 0,
            R.id.tuner_line_plus_10 to 10,
            R.id.tuner_line_plus_20 to 20,
            R.id.tuner_line_plus_30 to 30,
            R.id.tuner_line_plus_40 to 40,
            R.id.tuner_line_plus_50 to 50,
        )
        lineIds.forEach { (id,cents) ->
            val lineView = findViewById<ImageView>(id)
            tunerLineManager.addLine(cents, lineView)
        }
    }

    override fun onResume() {
        super.onResume()
        if (permissionManager.isMicrophonePermissionGranted()){
            audioProcessor.processAudio()
        } else if(permissionManager.shouldRequestMicrophonePermission()) {
            permissionManager.requestMicrophonePermission()
        } else {
            showPermissionRationale()
        }
    }

    private fun showPermissionRationale(){
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required_title)
            .setMessage(R.string.permission_rationale)
            .setPositiveButton(R.string.go_to_settings) { _, _->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)

                permissionManager.resetPermissionRequest()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
        noteTextView.text = getString(R.string.permission_not_granted)
        centerOffTextView.visibility = View.GONE
    }

    private fun startAudioProcessing() {
        centerOffTextView.visibility = View.VISIBLE
        audioProcessor.processAudio()
    }

    override fun onPause() {
        audioProcessor.stopProcessing()
        updateJob?.cancel()
        super.onPause()
    }

    override fun onPermissionGranted() {
        startAudioProcessing()
    }

    override fun onPermissionDenied() {
       showPermissionRationale()
    }

    override fun onPitchDetected(pitch: Note) {
        runOnUiThread{
            val displayText = pitch.note.noteDisplayed

            noteTextView.text = getString(
                R.string.detected_note,
                displayText,
                pitch.octave
            )

            centerOffTextView.text = getString(
                R.string.cents_off,
                pitch.centsOff
            )

            tunerLineManager.updateLines(pitch.centsOff)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioProcessor.stopProcessing()
    }
}