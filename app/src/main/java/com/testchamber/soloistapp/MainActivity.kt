package com.testchamber.soloistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.testchamber.soloistapp.core.navigation.AppNavigation
import com.testchamber.soloistapp.core.permissions.PermissionHandler
import com.testchamber.soloistapp.core.ui.theme.SoloistAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHandler = PermissionHandler(this)
        permissionHandler.checkAndRequestPermissions(
            onGranted = {
                enableEdgeToEdge()
                setContent {
                    SoloistAppTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            Surface(modifier = Modifier.padding(innerPadding)) {
                                AppNavigation()
                            }
                        }
                    }
                }
            },
            onDenied = {
                finish()
            },
        )
    }
}
