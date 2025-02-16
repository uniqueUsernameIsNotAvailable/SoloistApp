package com.testchamber.soloistapp.core.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.testchamber.soloistapp.domain.models.Track

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(
                track = track,
                onTrackSelect = onTrackSelect,
            )
        }
    }
}
