package com.testchamber.soloistapp.features.remote_music.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.testchamber.soloistapp.core.ComponentProvider
import com.testchamber.soloistapp.core.ui.components.MusicSearchBar
import com.testchamber.soloistapp.features.local_music.presentation.TrackList

@Composable
fun RemoteMusicScreen(
    onTrackSelect: (trackId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RemoteMusicViewModel =
        viewModel(
            factory = (LocalContext.current.applicationContext as ComponentProvider).provideViewModelFactory(),
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        MusicSearchBar(
            searchQuery =
                when (val state = uiState) {
                    is RemoteMusicUiState.Success -> state.searchQuery
                    else -> ""
                },
            onSearchQueryChange = { query ->
                viewModel.handleIntent(RemoteMusicIntent.SearchTracks(query))
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )

        when (val state = uiState) {
            is RemoteMusicUiState.Loading -> LoadingIndicator()
            is RemoteMusicUiState.Error -> ErrorMessage(state.message)
            is RemoteMusicUiState.Success -> {
                TrackList(
                    tracks = state.filteredTracks,
                    onTrackSelect = onTrackSelect,
                )
            }
        }
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
