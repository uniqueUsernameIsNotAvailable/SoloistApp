package com.testchamber.soloistapp.features.local_music.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.testchamber.soloistapp.core.ComponentProvider
import com.testchamber.soloistapp.core.ui.components.MusicSearchBar
import com.testchamber.soloistapp.core.ui.components.TrackList
import com.testchamber.soloistapp.core.ui.components.utils.ErrorMessage
import com.testchamber.soloistapp.core.ui.components.utils.LoadingIndicator
import com.testchamber.soloistapp.core.ui.components.utils.PermissionRequest

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
        when (val state = uiState) {
            is LocalMusicUiState.Loading -> LoadingIndicator()
            is LocalMusicUiState.Error -> ErrorMessage(state.message)
            is LocalMusicUiState.Success -> {
                MusicSearchBar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.handleIntent(LocalMusicIntent.UpdateSearchQuery(query))
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                )

                if (!state.isPermissionGranted) {
                    PermissionRequest { viewModel.handleIntent(LocalMusicIntent.RequestPermission) }
                } else {
                    TrackList(
                        tracks = state.filteredTracks,
                        onTrackSelect = onTrackSelect,
                    )
                }
            }
        }
    }
}
