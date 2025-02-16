package com.testchamber.soloistapp.features.remote_music.presentation

import com.testchamber.soloistapp.domain.models.Track

sealed interface RemoteMusicUiState {
    data object Loading : RemoteMusicUiState

    data class Error(
        val message: String,
    ) : RemoteMusicUiState

    data class Success(
        val tracks: List<Track>,
        val filteredTracks: List<Track>,
        val searchQuery: String,
    ) : RemoteMusicUiState
}

sealed interface RemoteMusicIntent {
    data object LoadTracks : RemoteMusicIntent

    data class SearchTracks(
        val query: String,
    ) : RemoteMusicIntent

    data class SelectTrack(
        val track: Track,
    ) : RemoteMusicIntent
}
