package com.testchamber.soloistapp.features.remote_music.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetRemoteTracksUseCase
import com.testchamber.soloistapp.domain.usecases.SearchRemoteTracksUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
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
        private var searchJob: Job? = null
        private var currentQuery = ""

        init {
            loadTracks()
        }

        fun handleIntent(intent: RemoteMusicIntent) {
            when (intent) {
                is RemoteMusicIntent.LoadTracks -> loadTracks()
                is RemoteMusicIntent.SearchTracks -> searchTracks(intent.query)
                is RemoteMusicIntent.ClearSearch -> clearSearch()
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
                            searchQuery = currentQuery,
                            isSearchActive = false,
                        )
                } catch (e: Exception) {
                    _uiState.value = RemoteMusicUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun searchTracks(query: String) {
            currentQuery = query

            (_uiState.value as? RemoteMusicUiState.Success)?.let { currentState ->
                _uiState.value =
                    currentState.copy(
                        searchQuery = query,
                        isSearchActive = query.isNotBlank(),
                    )
            }

            if (query.isBlank()) {
                clearSearch()
                return
            }

            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    try {
                        delay(500)

                        if (query != currentQuery) return@launch

                        val searchResults = searchRemoteTracksUseCase.execute(query)
                        if (query == currentQuery) {
                            _uiState.value = (_uiState.value as? RemoteMusicUiState.Success)?.copy(
                                filteredTracks = searchResults,
                                searchQuery = query,
                                isSearchActive = true,
                            ) ?: RemoteMusicUiState.Success(
                                tracks = allTracks,
                                filteredTracks = searchResults,
                                searchQuery = query,
                                isSearchActive = true,
                            )
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            return@launch
                        }
                        if (query == currentQuery) {
                            _uiState.value = RemoteMusicUiState.Error(e.message ?: "Unknown error")
                        }
                    }
                }
        }

        private fun clearSearch() {
            currentQuery = ""
            searchJob?.cancel()
            (_uiState.value as? RemoteMusicUiState.Success)?.let { currentState ->
                _uiState.value =
                    currentState.copy(
                        filteredTracks = allTracks,
                        searchQuery = "",
                        isSearchActive = false,
                    )
            }
        }

        private fun selectTrack(track: Track) {
        }

        override fun onCleared() {
            super.onCleared()
            searchJob?.cancel()
        }
    }
