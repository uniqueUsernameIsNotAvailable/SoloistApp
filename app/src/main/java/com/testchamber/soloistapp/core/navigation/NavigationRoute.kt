package com.testchamber.soloistapp.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

@Serializable
data object RemoteMusic

@Serializable
data object LocalMusic

@Serializable
data class MusicPlayer(
    val trackId: String,
    val isRemote: Boolean,
)

data class TopLevelRoute<T : Any>(
    val name: String,
    val icon: ImageVector,
    val screen: T,
)

val topLevelRoutes =
    listOf(
        TopLevelRoute(
            name = "Local",
            icon = Icons.Default.Home,
            screen = LocalMusic,
        ),
        TopLevelRoute(
            name = "Remote",
            icon = Icons.Filled.AddCircle,
            screen = RemoteMusic,
        ),
    )
