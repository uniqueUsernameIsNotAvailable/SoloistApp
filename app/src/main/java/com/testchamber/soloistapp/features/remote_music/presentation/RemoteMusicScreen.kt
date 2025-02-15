package com.testchamber.soloistapp.features.remote_music.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RemoteMusicScreen(
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Online Music Screen")
        Button(onClick = { onTrackSelect("remote_track_322") }) {
            Text("Play Remote Track")
        }
    }
}
