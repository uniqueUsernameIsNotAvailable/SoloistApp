package com.testchamber.soloistapp.features.local_music.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LocalMusicScreen(
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("My Music Screen")
        Button(onClick = { onTrackSelect("local_track_1337") }) {
            Text("Play My Track")
        }
    }
}
