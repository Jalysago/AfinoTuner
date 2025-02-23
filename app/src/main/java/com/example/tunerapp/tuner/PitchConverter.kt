package com.example.tunerapp.tuner

import kotlin.math.log2
import kotlin.math.roundToInt

class PitchConverter(private var referenceFreq: Float = 440f) {
    fun hzToNote(freq: Float ): Note?{
        if(freq <= 0) return null
        val midiNumber = 69 + 12 * log2(freq / referenceFreq)
        val nearestMidi = midiNumber.roundToInt()
        val centsOff = (midiNumber - nearestMidi) * 100
        val chromaticScale = ChromaticScale.midiToChromatic(nearestMidi)
        val octave = (nearestMidi / 12) - 1
        return Note(chromaticScale, octave, centsOff)
    }
}