package com.example.tunerapp.tuner

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

class AudioProcessor (private val pitchListener: PitchListener) {
    private val sampleRate = 44100
    private val bufferSize = 4096
    private val overlap = 0
    private lateinit var dispatcher: AudioDispatcher

    fun processAudio() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate,bufferSize,overlap)
        val pitchHandler = PitchDetectionHandler { result, _->
            pitchListener.onPitchDetected(result.pitch)
        }
        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.MPM,
            sampleRate.toFloat(),
            bufferSize,
            pitchHandler
        )
        dispatcher.addAudioProcessor(pitchProcessor)
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    fun stopProcessing() {
        dispatcher.stop()
    }
}

