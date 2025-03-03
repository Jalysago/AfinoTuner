package com.example.tunerapp.tuner

interface PermissionResultCallback {
    fun onPermissionGranted()
    fun onPermissionDenied()
}