package com.testchamber.soloistapp.features.local_music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetLocalTracksUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocalMusicViewModel
    @Inject
    constructor(
        private val getLocalTracksUseCase: GetLocalTracksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LocalMusicUiState>(LocalMusicUiState.Loading)
        val uiState: StateFlow<LocalMusicUiState> = _uiState.asStateFlow()

        private var allTracks = listOf<Track>()

        init {
            loadTracks()
        }

        fun handleIntent(intent: LocalMusicIntent) {
            when (intent) {
                is LocalMusicIntent.LoadTracks -> loadTracks()
                is LocalMusicIntent.RequestPermission -> requestPermission()
                is LocalMusicIntent.SelectTrack -> selectTrack(intent.track)
                is LocalMusicIntent.UpdateSearchQuery -> updateSearchQuery(intent.query)
                is LocalMusicIntent.ClearSearch -> clearSearch()
            }
        }

        private fun loadTracks() {
            viewModelScope.launch {
                try {
                    _uiState.value = LocalMusicUiState.Loading
                    allTracks = getLocalTracksUseCase.execute()
                    _uiState.value =
                        LocalMusicUiState.Success(
                            tracks = allTracks,
                            filteredTracks = allTracks,
                            searchQuery = "",
                            isPermissionGranted = true,
                        )
                } catch (e: Exception) {
                    _uiState.value = LocalMusicUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun updateSearchQuery(query: String) {
            if (query.isBlank()) {
                clearSearch()
                return
            }

            val currentState = _uiState.value
            if (currentState is LocalMusicUiState.Success) {
                val filtered =
                    allTracks.filter { track ->
                        track.title.contains(query, ignoreCase = true) ||
                            track.artist.contains(query, ignoreCase = true)
                    }
                _uiState.value =
                    currentState.copy(
                        filteredTracks = filtered,
                        searchQuery = query,
                    )
            }
        }

        private fun clearSearch() {
            val currentState = _uiState.value
            if (currentState is LocalMusicUiState.Success) {
                _uiState.value =
                    currentState.copy(
                        filteredTracks = allTracks,
                        searchQuery = "",
                    )
            }
        }
    }

private fun requestPermission() {
    // TODO
}

private fun selectTrack(track: Track) {
    // TODO
}
