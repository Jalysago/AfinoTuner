package com.example.tunerapp.utilities

import android.content.Context
import android.content.SharedPreferences
import com.example.tunerapp.tuner.PitchDetectionAlgorithm

class SettingsManager(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    fun getTuningFrequency(): Float {
        return sharedPrefs.getFloat(KEY_TUNING_FREQUENCY, DEFAULT_TUNING_FREQUENCY)
    }

    fun setTuningFrequency(frequency: Float): Boolean {
        if (frequency < MIN_TUNING_FREQUENCY || frequency > MAX_TUNING_FREQUENCY) {
            return false
        }
        return sharedPrefs.edit().putFloat(KEY_TUNING_FREQUENCY,frequency).commit()
    }
    
    fun getAlgorithmOrdinal(): Int {
        return sharedPrefs.getInt(KEY_ALGORITHM, PitchDetectionAlgorithm.getDefault().ordinal)
    }

    fun getAlgorithm(): PitchDetectionAlgorithm {
        val ordinal = getAlgorithmOrdinal()
        return try {
            PitchDetectionAlgorithm.entries[ordinal]
        } catch (e: Exception) {
            PitchDetectionAlgorithm.getDefault()
        }
    }

    fun setAlgorithm(algorithm: PitchDetectionAlgorithm): Boolean {
        return sharedPrefs.edit().putInt(KEY_ALGORITHM,algorithm.ordinal).commit()
    }

    fun getMinTuningFrequency(): Float = MIN_TUNING_FREQUENCY
    fun getMaxTuningFrequency(): Float = MAX_TUNING_FREQUENCY

    companion object {
        private const val PREFS_NAME = "tuner_settings"
        private const val KEY_TUNING_FREQUENCY = "tuning_frequency"
        private const val KEY_ALGORITHM = "pitch_algorithm"

        private const val DEFAULT_TUNING_FREQUENCY = 440f
        private const val MIN_TUNING_FREQUENCY = 400f
        private const val MAX_TUNING_FREQUENCY = 500f
    }
}