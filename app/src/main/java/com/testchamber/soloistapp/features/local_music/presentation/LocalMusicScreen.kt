package com.testchamber.soloistapp.features.local_music.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.testchamber.soloistapp.R
import com.testchamber.soloistapp.core.ComponentProvider
import com.testchamber.soloistapp.domain.models.Track
import java.util.concurrent.TimeUnit

@Composable
fun LocalMusicScreen(
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocalMusicViewModel =
        viewModel(
            factory = (LocalContext.current.applicationContext as ComponentProvider).provideViewModelFactory(),
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            onSearchQueryChange = { query ->
                viewModel.handleIntent(LocalMusicIntent.UpdateSearchQuery(query))
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )

        when (uiState) {
            is LocalMusicUiState.Loading -> LoadingIndicator()
            is LocalMusicUiState.Error -> ErrorMessage((uiState as LocalMusicUiState.Error).message)
            is LocalMusicUiState.Success -> {
                val successState = uiState as LocalMusicUiState.Success
                TrackList(
                    tracks = successState.filteredTracks,
                    onTrackSelect = onTrackSelect,
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onSearchQueryChange(newText)
        },
        modifier = modifier,
        placeholder = { Text("Search tracks...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        singleLine = true,
    )
}

@Composable
private fun LocalMusicContent(
    uiState: LocalMusicUiState,
    onTrackSelect: (trackId: String) -> Unit,
    onPermissionRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is LocalMusicUiState.Loading -> LoadingIndicator()
            is LocalMusicUiState.Error -> ErrorMessage(uiState.message)
            is LocalMusicUiState.Success -> {
                if (!uiState.isPermissionGranted) {
                    PermissionRequest(onPermissionRequest)
                } else {
                    TrackList(
                        tracks = uiState.tracks,
                        onTrackSelect = onTrackSelect,
                    )
                }
            }
        }
    }
}

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

@Composable
fun TrackListItem(
    track: Track,
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onTrackSelect(track.id) }
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // art
        AsyncImage(
            model = track.coverArtUri,
            contentDescription = "Album cover for ${track.title}",
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_music_note_24dp),
        )

        // Track data
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = formatDuration(track.duration),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun PermissionRequest(onPermissionRequest: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Storage permission is required to access local music",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onPermissionRequest) {
            Text("Grant Permission")
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds =
        TimeUnit.MILLISECONDS.toSeconds(durationMs) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}
