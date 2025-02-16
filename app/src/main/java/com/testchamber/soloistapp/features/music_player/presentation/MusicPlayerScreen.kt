package com.testchamber.soloistapp.features.music_player.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MusicPlayerScreen(
    trackId: String,
    isRemote: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Now Playing: $trackId")
    }
}
