package com.testchamber.soloistapp.features.music_player.presentation

import com.testchamber.soloistapp.domain.models.Track

sealed interface MusicPlayerUiState {
    data object Loading : MusicPlayerUiState

    data class Error(
        val message: String,
    ) : MusicPlayerUiState

    data class Playing(
        val track: Track,
        val isPlaying: Boolean = false,
        val currentPosition: Long = 0L,
        val bufferedPosition: Long = 0L,
        val duration: Long = 0L,
    ) : MusicPlayerUiState
}

sealed interface MusicPlayerIntent {
    data object PlayPause : MusicPlayerIntent

    data object Next : MusicPlayerIntent

    data object Previous : MusicPlayerIntent

    data class SeekTo(
        val position: Long,
    ) : MusicPlayerIntent
}
