package com.testchamber.soloistapp.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.testchamber.soloistapp.features.local_music.presentation.LocalMusicScreen
import com.testchamber.soloistapp.features.music_player.presentation.MusicPlayerScreen
import com.testchamber.soloistapp.features.remote_music.presentation.RemoteMusicScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTopLevelRoute =
        currentRoute in
            setOf(
                RemoteMusic::class.qualifiedName,
                LocalMusic::class.qualifiedName,
            )

    Scaffold(
        bottomBar = { if (isTopLevelRoute) BottomNavigationBar(navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = LocalMusic,
        ) {
            composable<RemoteMusic> {
                RemoteMusicScreen(
                    modifier = Modifier.padding(paddingValues),
                    onTrackSelect = { trackId ->
                        navController.navigate(MusicPlayer(trackId))
                    },
                )
            }
            composable<LocalMusic> {
                LocalMusicScreen(
                    modifier = Modifier.padding(paddingValues),
                    onTrackSelect = { trackId ->
                        navController.navigate(MusicPlayer(trackId))
                    },
                )
            }
            composable<MusicPlayer> { backStackEntry ->
                val args = backStackEntry.toRoute<MusicPlayer>()
                MusicPlayerScreen(
                    modifier = Modifier.padding(paddingValues),
                    trackId = args.trackId,
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    NavigationBar {
        topLevelRoutes.forEach { route ->
            val isSelected = currentRoute == route.screen::class.qualifiedName

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(route.screen) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.name,
                    )
                },
                label = { Text(route.name) },
            )
        }
    }
}
