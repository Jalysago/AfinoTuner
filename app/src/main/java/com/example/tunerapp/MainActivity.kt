package com.example.tunerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.tunerapp.tuner.AudioProcessor
import com.example.tunerapp.tuner.Note
import com.example.tunerapp.tuner.PermissionManager
import com.example.tunerapp.tuner.PermissionResultCallback
import com.example.tunerapp.tuner.PitchDetectionAlgorithm
import com.example.tunerapp.tuner.PitchListener
import com.example.tunerapp.utilities.SettingsManager
import com.example.tunerapp.utilities.TunerLineManager
import kotlinx.coroutines.Job


class MainActivity : AppCompatActivity(), PitchListener, PermissionResultCallback {

    private lateinit var noteTextView: TextView
    private lateinit var centerOffTextView: TextView
    private lateinit var tuningStandardTextView: TextView
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var permissionManager: PermissionManager
    private lateinit var tunerLineManager: TunerLineManager
    private lateinit var settingsManager: SettingsManager

    private var updateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        noteTextView = findViewById(R.id.activity_main_note_textView)
        centerOffTextView = findViewById(R.id.cents_off_textView)
        tuningStandardTextView = findViewById(R.id.tuning_standard_textView)

        settingsManager = SettingsManager(this)
        audioProcessor = AudioProcessor(this)
        permissionManager = PermissionManager(this)
        permissionManager.register(this)
        tunerLineManager = TunerLineManager(this)

        setupTunerLines()
        updateTuningStandardDisplay()
        updateAudioProcessorSettings()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsDialog()
                true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val frequencyEditText = dialogView.findViewById<EditText>(R.id.tuning_frequency_editText)
        val algorithmSpinner = dialogView.findViewById<Spinner>(R.id.spinner_algorithm)

        frequencyEditText.setText(settingsManager.getTuningFrequency().toString())

        //this sets the spinner with all the algorithms available
        val algorithms = PitchDetectionAlgorithm.entries.map {it.displayName}.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, algorithms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        algorithmSpinner.adapter = adapter
        algorithmSpinner.setSelection(settingsManager.getAlgorithmOrdinal())

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) {_, _->
                try {
                    val frequency = frequencyEditText.text.toString().toFloat()
                    val minFreq = settingsManager.getMinTuningFrequency()
                    val maxFreq = settingsManager.getMaxTuningFrequency()

                    if (frequency < minFreq || frequency > maxFreq) {
                        Toast.makeText(
                            this,
                            getString(R.string.invalid_frequency, minFreq, maxFreq),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    }
                    settingsManager.setTuningFrequency(frequency)

                    val selectedAlgorithm = PitchDetectionAlgorithm.entries[algorithmSpinner.selectedItemPosition]
                    settingsManager.setAlgorithm(selectedAlgorithm)

                    updateTuningStandardDisplay()
                    updateAudioProcessorSettings()

                    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        getString(R.string.invalid_frequency,
                            settingsManager.getMinTuningFrequency(),
                            settingsManager.getMaxTuningFrequency()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun updateTuningStandardDisplay() {
        tuningStandardTextView.text = getString(
            R.string.current_tuning_standard,
            settingsManager.getTuningFrequency()
        )
    }

    private fun updateAudioProcessorSettings() {
        audioProcessor.updateSettings(
            settingsManager.getTuningFrequency(),
            settingsManager.getAlgorithm()
        )
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