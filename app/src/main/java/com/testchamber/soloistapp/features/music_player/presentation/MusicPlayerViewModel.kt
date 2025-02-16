package com.testchamber.soloistapp.features.music_player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.core.di.AssistedSavedStateViewModelFactory
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetRemoteTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel
    @AssistedInject
    constructor(
        private val getRemoteTrackUseCase: GetRemoteTrackUseCase,
        private val mediaPlayer: MediaPlayer,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory : AssistedSavedStateViewModelFactory<MusicPlayerViewModel> {
            override fun create(savedStateHandle: SavedStateHandle): MusicPlayerViewModel
        }

        private val _uiState = MutableStateFlow<MusicPlayerUiState>(MusicPlayerUiState.Loading)
        val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

        private var currentTrack: Track? = null
        private val trackId: String = checkNotNull(savedStateHandle["trackId"])

        init {
            loadTrack()
            observePlaybackState()
        }

        private fun loadTrack() {
            viewModelScope.launch {
                try {
                    val track = getRemoteTrackUseCase.execute(trackId)
                    currentTrack = track
                    mediaPlayer.prepare(track.uri)
                    _uiState.value = MusicPlayerUiState.Playing(track = track)
                } catch (e: Exception) {
                    _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun observePlaybackState() {
            viewModelScope.launch {
                mediaPlayer.playbackState.collect { state ->
                    currentTrack?.let { track ->
                        _uiState.value =
                            MusicPlayerUiState.Playing(
                                track = track,
                                isPlaying = state.isPlaying,
                                currentPosition = state.currentPosition,
                                bufferedPosition = state.bufferedPosition,
                                duration = state.duration,
                            )
                    }
                }
            }
        }

        fun handleIntent(intent: MusicPlayerIntent) {
            when (intent) {
                is MusicPlayerIntent.PlayPause -> togglePlayPause()
                is MusicPlayerIntent.SeekTo -> seekTo(intent.position)
                is MusicPlayerIntent.Next -> {} // TODO
                is MusicPlayerIntent.Previous -> {} // TODO
            }
        }

        private fun togglePlayPause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.play()
            }
        }

        private fun seekTo(position: Long) {
            mediaPlayer.seekTo(position)
        }

        override fun onCleared() {
            super.onCleared()
            mediaPlayer.release()
        }
    }
