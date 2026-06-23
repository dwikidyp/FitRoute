package com.fitroute.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object PermissionHelper {

    // Cek apakah izin fine location sudah diberikan
    fun hasFineLocationPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Cek apakah izin background location sudah diberikan
    fun hasBackgroundLocationPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else true // Android 9 ke bawah tidak butuh izin ini
    }

    // Minta izin fine location dulu, baru background
    fun requestLocationPermissions(
        activity: AppCompatActivity,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { granted ->
            if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                // Fine location granted → minta background location
                requestBackgroundLocationIfNeeded(activity, onGranted, onDenied)
            } else {
                onDenied()
            }
        }

        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )
    }

    // Minta background location (Android 10+)
    private fun requestBackgroundLocationIfNeeded(
        activity: AppCompatActivity,
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (hasBackgroundLocationPermission(activity)) {
                onGranted()
            } else {
                activity.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) onGranted() else onDenied()
                }.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else {
            onGranted()
        }
    }
}