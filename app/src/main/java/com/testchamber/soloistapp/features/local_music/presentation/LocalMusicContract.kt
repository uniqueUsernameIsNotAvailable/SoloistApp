package com.testchamber.soloistapp.features.local_music.presentation

import com.testchamber.soloistapp.domain.models.Track

sealed interface LocalMusicUiState {
    data object Loading : LocalMusicUiState

    data class Error(
        val message: String,
    ) : LocalMusicUiState

    data class Success(
        val tracks: List<Track>,
        val filteredTracks: List<Track>,
        val searchQuery: String,
        val isPermissionGranted: Boolean,
    ) : LocalMusicUiState
}

sealed interface LocalMusicIntent {
    data object LoadTracks : LocalMusicIntent

    data object RequestPermission : LocalMusicIntent

    data class SelectTrack(
        val track: Track,
    ) : LocalMusicIntent

    data class UpdateSearchQuery(
        val query: String,
    ) : LocalMusicIntent

    data object ClearSearch : LocalMusicIntent
}
