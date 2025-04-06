package com.example.tunerapp.tuner

import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm

enum class PitchDetectionAlgorithm(
    val displayName: String,
    val algorithm: PitchEstimationAlgorithm
) {
    MCLEOD("McLeod (MPM)", PitchProcessor.PitchEstimationAlgorithm.MPM),
    YIN("Yin", PitchProcessor.PitchEstimationAlgorithm.YIN),
    FFT_YIN("FFT + Yin", PitchProcessor.PitchEstimationAlgorithm.FFT_YIN),
    DYNAMIC_WAVELET("Dynamic Wavelet", PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET),
    AMDF("AMDF", PitchProcessor.PitchEstimationAlgorithm.AMDF);

    companion object {
        fun getDefault(): PitchDetectionAlgorithm = MCLEOD
    }
}