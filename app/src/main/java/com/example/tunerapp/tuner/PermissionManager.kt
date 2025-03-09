package com.example.tunerapp.tuner

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: AppCompatActivity) {

    private val sharedPrefs: SharedPreferences by lazy {
        activity.getSharedPreferences("tuner_prefs", Context.MODE_PRIVATE)
    }

    lateinit var permissionLauncher: ActivityResultLauncher<String>
        private set

    private val PREF_PERMISSION_REQUESTED = "mic_permission_requested"

    fun register(callback: PermissionResultCallback) {
        permissionLauncher = activity.registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                sharedPrefs.edit().putBoolean(PREF_PERMISSION_REQUESTED, false).apply()
                callback.onPermissionGranted()
            } else {
                sharedPrefs.edit().putBoolean(PREF_PERMISSION_REQUESTED, true).apply()
                callback.onPermissionDenied()
            }
        }
    }

    fun isMicrophonePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldRequestMicrophonePermission(): Boolean {
        val alreadyAsked = sharedPrefs.getBoolean(PREF_PERMISSION_REQUESTED, false)
        return !isMicrophonePermissionGranted() && !alreadyAsked
    }

    fun resetPermissionRequest() {
        sharedPrefs.edit().putBoolean(PREF_PERMISSION_REQUESTED, false).apply()
    }

    fun requestMicrophonePermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}