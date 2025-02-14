package com.example.tunerapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tunerapp.tuner.AudioProcessor
import com.example.tunerapp.tuner.PitchListener

class MainActivity : ComponentActivity(), PitchListener {

    private lateinit var pitchDisplayTextView:TextView
    private lateinit var audioProcessor: AudioProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pitchDisplayTextView = findViewById(R.id.activity_main_pitch_textView)
        audioProcessor = AudioProcessor(this)
    }

    override fun onResume() {
        super.onResume()
        audioProcessor.processAudio()
    }

    override fun onPause() {
        audioProcessor.stopProcessing()
        super.onPause()
    }

    override fun onPitchDetected(pitch: Float) {
        runOnUiThread{
            pitchDisplayTextView.text = getString(R.string.hertz, pitch)
        }
    }
}

