package com.testchamber.soloistapp.features.music_player.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.testchamber.soloistapp.App
import com.testchamber.soloistapp.R
import com.testchamber.soloistapp.core.ui.components.utils.ErrorMessage
import com.testchamber.soloistapp.core.ui.components.utils.LoadingIndicator
import com.testchamber.soloistapp.core.utils.formatMusicDuration

@Composable
fun MusicPlayerScreen(
    trackId: String,
    isRemote: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as App
    val viewModelFactory =
        remember(trackId, isRemote) {
            app.appComponent
                .musicPlayerViewModelFactoryProvider()
                .create(trackId, isRemote)
        }

    val viewModel: MusicPlayerViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is MusicPlayerUiState.Loading -> LoadingIndicator()
        is MusicPlayerUiState.Error -> ErrorMessage(state.message)
        is MusicPlayerUiState.Playing ->
            PlayerContent(
                state = state,
                onPlayPause = { viewModel.handleIntent(MusicPlayerIntent.PlayPause) },
                onSeek = { position -> viewModel.handleIntent(MusicPlayerIntent.SeekTo(position)) },
                onNext = { viewModel.handleIntent(MusicPlayerIntent.Next) },
                onPrevious = { viewModel.handleIntent(MusicPlayerIntent.Previous) },
                modifier = modifier,
            )
    }
}

@Composable
private fun PlayerContent(
    state: MusicPlayerUiState.Playing,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // img
        AsyncImage(
            model = state.track.coverArtUri,
            contentDescription = "Album cover for ${state.track.title}",
            modifier =
                Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_music_note_24dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Track data
        Text(
            text = state.track.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = state.track.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress
        Column(modifier = Modifier.fillMaxWidth()) {
            if (state.duration > 0) {
                Slider(
                    value = state.currentPosition.coerceIn(0, state.duration).toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..state.duration.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = (state.currentPosition.coerceAtLeast(0)).formatMusicDuration(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = (state.duration.coerceAtLeast(0)).formatMusicDuration(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // control panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous track",
                    modifier = Modifier.size(48.dp),
                )
            }

            IconButton(
                onClick = onPlayPause,
                modifier =
                    Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ).padding(8.dp),
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.ArrowDropDown else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next track",
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}
