package com.example.tunerapp.tuner

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class AudioProcessor (
    private val pitchListener: PitchListener,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ) {
    private val sampleRate = 44100
    private val bufferSize = 4096
    private val overlap = 3072
    private var dispatcher: AudioDispatcher? = null
    private var isProcessing = false

    private var currentAlgorithm: PitchDetectionAlgorithm = PitchDetectionAlgorithm.getDefault()
    private var pitchConverter = PitchConverter()

    fun updateSettings(tuningFrequency: Float, algorithm: PitchDetectionAlgorithm) {
        pitchConverter = PitchConverter(tuningFrequency)

        if (currentAlgorithm != algorithm && isProcessing) {
            currentAlgorithm = algorithm
            stopProcessing()
            processAudio()
        } else {
            currentAlgorithm = algorithm
        }
    }

    fun processAudio() {
        if (isProcessing) return

        isProcessing = true
        coroutineScope.launch {
            try {
                val newDispatcher = withContext(Dispatchers.IO) {
                    AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap)
                }
                dispatcher = newDispatcher
                val pitchHandler = PitchDetectionHandler { result, _->
                    if(result.pitch > 0) {
                        val note = pitchConverter.hzToNote(result.pitch)
                        note?.let {//this helps avoiding false positives.
                            if (result.probability > 0.85) {
                                pitchListener.onPitchDetected(it)
                            }
                        }
                    }
                }
                val pitchProcessor = PitchProcessor(
                    currentAlgorithm.algorithm,
                    sampleRate.toFloat(),
                    bufferSize,
                    pitchHandler
                )

                newDispatcher.addAudioProcessor(pitchProcessor)
                Thread(newDispatcher, "Audio Dispatcher").apply {
                    priority = Thread.MAX_PRIORITY
                    start()
                }
            } catch (e: Exception) {
                isProcessing = false
                Log.e("AudioProcessor", "Error Initializing audio dispatcher", e)
            }
        }
    }
    fun stopProcessing() {
        dispatcher?.let{
            try {
                it.stop()
                dispatcher = null
                isProcessing = false
            } catch (e: Exception) {
                Log.e("AudioProcessor", "Error Stopping audio dispatcher",e )
            }
        }
    }
}