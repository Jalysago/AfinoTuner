package com.example.tunerapp.tuner

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: AppCompatActivity) {

    lateinit var permissionLauncher: ActivityResultLauncher<String>
        private set

    fun register(callback: PermissionResultCallback) {
        permissionLauncher = activity.registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                callback.onPermissionGranted()
            } else {
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

    fun requestMicrophonePermission(){
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}