package com.example.tunerapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tunerapp.tuner.AudioProcessor
import com.example.tunerapp.tuner.Note
import com.example.tunerapp.tuner.PermissionManager
import com.example.tunerapp.tuner.PermissionResultCallback
import com.example.tunerapp.tuner.PitchListener

class MainActivity : AppCompatActivity(), PitchListener, PermissionResultCallback {

    private lateinit var pitchDisplayTextView:TextView
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pitchDisplayTextView = findViewById(R.id.activity_main_pitch_textView)
        audioProcessor = AudioProcessor(this)
        permissionManager = PermissionManager(this)
        permissionManager.register(this)
    }

    override fun onResume() {
        super.onResume()
        if (permissionManager.isMicrophonePermissionGranted()){
            audioProcessor.processAudio()
        } else {
            permissionManager.requestMicrophonePermission()
        }
    }

    override fun onPause() {
        audioProcessor.stopProcessing()
        super.onPause()
    }

    override fun onPermissionGranted() {
        audioProcessor.processAudio()
    }

    override fun onPermissionDenied() {
        runOnUiThread{
            pitchDisplayTextView.text = getString(R.string.permission_not_granted)
        }
    }

    override fun onPitchDetected(pitch: Note) {
        runOnUiThread{
            pitchDisplayTextView.text = getString(
                R.string.detected_pitch,
                pitch.note.noteDisplayed,
                pitch.octave,
                pitch.centsOff)
        }
    }
}