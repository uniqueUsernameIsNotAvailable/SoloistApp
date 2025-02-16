package com.testchamber.soloistapp.features.music_player.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import com.testchamber.soloistapp.features.music_player.service.MediaServiceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel(
    private val getTrackUseCase: GetTrackUseCase,
    private val mediaController: MediaServiceController,
    private val playlistManager: PlaylistManager,
    private val trackId: String,
    private val isRemote: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MusicPlayerUiState>(MusicPlayerUiState.Loading)
    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

    init {
        initializePlaylist()
        observePlaybackState()
        observeCurrentTrack()
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            mediaController.playbackState.collect { playbackState ->
                val currentState = _uiState.value
                if (currentState is MusicPlayerUiState.Playing) {
                    _uiState.value =
                        currentState.copy(
                            isPlaying = playbackState.isPlaying,
                            currentPosition = playbackState.currentPosition,
                            bufferedPosition = playbackState.bufferedPosition,
                            duration = playbackState.duration,
                        )
                }
            }
        }
    }

    private fun observeCurrentTrack() {
        viewModelScope.launch {
            mediaController.currentTrack.collect { track ->
                track?.let {
                    _uiState.value = MusicPlayerUiState.Playing(track = it)
                }
            }
        }
    }

    private fun initializePlaylist() {
        viewModelScope.launch {
            try {
                mediaController.release()

                playlistManager.initializePlaylist(trackId, isRemote)
                loadCurrentTrack()
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error initializing playlist", e)
                _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadCurrentTrack() {
        viewModelScope.launch {
            try {
                playlistManager.getCurrentTrack()?.let { track ->
                    _uiState.value = MusicPlayerUiState.Playing(track = track)
                    mediaController.playTrack(track)
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error loading track", e)
                _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun handleIntent(intent: MusicPlayerIntent) {
        viewModelScope.launch {
            try {
                when (intent) {
                    is MusicPlayerIntent.PlayPause -> mediaController.togglePlayPause()
                    is MusicPlayerIntent.SeekTo -> mediaController.seekTo(intent.position)
                    is MusicPlayerIntent.Next -> {
                        mediaController.next()
                    }

                    is MusicPlayerIntent.Previous -> {
                        mediaController.previous()
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error handling intent: $intent", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try {
                mediaController.release()
            } catch (e: Exception) {
                Log.e("MusicPlayerViewModel", "Error clearing ViewModel", e)
            }
        }
    }
}
