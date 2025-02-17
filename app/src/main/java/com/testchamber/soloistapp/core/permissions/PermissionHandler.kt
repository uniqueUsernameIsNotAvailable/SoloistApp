package com.testchamber.soloistapp.core.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHandler(
    private val activity: ComponentActivity,
) {
    private val requestPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            handlePermissionResult(permissions)
        }

    private var onPermissionsGranted: () -> Unit = {}
    private var onPermissionsDenied: () -> Unit = {
        Toast
            .makeText(
                activity,
                "MISSING PERMISSIONS.",
                Toast.LENGTH_LONG,
            ).show()
    }

    fun checkAndRequestPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit = onPermissionsDenied,
    ) {
        onPermissionsGranted = onGranted
        onPermissionsDenied = onDenied

        if (!hasRequiredPermissions()) {
            requestPermissions()
        } else {
            onPermissionsGranted()
        }
    }

    private fun hasRequiredPermissions(): Boolean =
        getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                activity,
                permission,
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun getRequiredPermissions(): List<String> =
        mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    private fun requestPermissions() {
        val missingPermissions =
            getRequiredPermissions().filter { permission ->
                ContextCompat.checkSelfPermission(
                    activity,
                    permission,
                ) != PackageManager.PERMISSION_GRANTED
            }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            onPermissionsGranted()
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }
}
