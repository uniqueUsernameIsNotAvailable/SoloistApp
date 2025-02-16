package com.testchamber.soloistapp.features.music_player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel
    @Inject
    constructor(
        private val getTrackUseCase: GetTrackUseCase,
        private val mediaPlayer: MediaPlayer,
        private val trackId: String,
        private val isRemote: Boolean,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MusicPlayerUiState>(MusicPlayerUiState.Loading)
        val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

        private var currentTrack: Track? = null
        private var playbackJob: Job? = null

        init {
            loadTrack()
            observePlaybackState()
        }

        private fun loadTrack() {
            viewModelScope.launch {
                try {
                    // Cancel any existing playback observation
                    playbackJob?.cancel()

                    val track = getTrackUseCase.execute(trackId, isRemote)
                    currentTrack = track
                    mediaPlayer.prepare(track.uri)
                    _uiState.value = MusicPlayerUiState.Playing(track = track)
                } catch (e: Exception) {
                    _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun observePlaybackState() {
            playbackJob?.cancel()
            playbackJob =
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

        override fun onCleared() {
            super.onCleared()
            playbackJob?.cancel()
            mediaPlayer.release()
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
    }
