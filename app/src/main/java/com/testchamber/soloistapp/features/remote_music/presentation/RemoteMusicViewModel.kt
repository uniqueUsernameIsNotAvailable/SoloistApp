package com.testchamber.soloistapp.features.remote_music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetRemoteTracksUseCase
import com.testchamber.soloistapp.domain.usecases.SearchRemoteTracksUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteMusicViewModel
    @Inject
    constructor(
        private val getRemoteTracksUseCase: GetRemoteTracksUseCase,
        private val searchRemoteTracksUseCase: SearchRemoteTracksUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<RemoteMusicUiState>(RemoteMusicUiState.Loading)
        val uiState: StateFlow<RemoteMusicUiState> = _uiState.asStateFlow()

        private var allTracks = listOf<Track>()
        private var currentSearchQuery = ""
        private var searchJob: Job? = null

        init {
            loadTracks()
        }

        fun handleIntent(intent: RemoteMusicIntent) {
            when (intent) {
                is RemoteMusicIntent.LoadTracks -> loadTracks()
                is RemoteMusicIntent.SearchTracks -> searchTracks(intent.query)
                is RemoteMusicIntent.SelectTrack -> selectTrack(intent.track)
            }
        }

        private fun loadTracks() {
            viewModelScope.launch {
                try {
                    _uiState.value = RemoteMusicUiState.Loading
                    allTracks = getRemoteTracksUseCase.execute()
                    _uiState.value =
                        RemoteMusicUiState.Success(
                            tracks = allTracks,
                            filteredTracks = allTracks,
                            searchQuery = currentSearchQuery,
                        )
                } catch (e: Exception) {
                    _uiState.value = RemoteMusicUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun searchTracks(query: String) {
            currentSearchQuery = query
            searchJob?.cancel()
            if (query.isBlank()) {
                _uiState.value = (_uiState.value as? RemoteMusicUiState.Success)?.copy(
                    filteredTracks = allTracks,
                    searchQuery = query,
                ) ?: return
                return
            }

            searchJob =
                viewModelScope.launch {
                    try {
                        delay(500)
                        val searchResults = searchRemoteTracksUseCase.execute(query)
                        _uiState.value =
                            RemoteMusicUiState.Success(
                                tracks = allTracks,
                                filteredTracks = searchResults,
                                searchQuery = query,
                            )
                    } catch (e: Exception) {
                        _uiState.value = RemoteMusicUiState.Error(e.message ?: "Unknown error")
                    }
                }
        }

        private fun selectTrack(track: Track) {
            // TODO
        }
    }
