package com.testchamber.soloistapp.features.music_player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
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
        private val playlistManager: PlaylistManager,
        private val trackId: String,
        private val isRemote: Boolean,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MusicPlayerUiState>(MusicPlayerUiState.Loading)
        val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

        private var currentTrack: Track? = null
        private var playbackJob: Job? = null

        init {
            initializePlaylist()
        }

        private fun initializePlaylist() {
            viewModelScope.launch {
                try {
                    playlistManager.initializePlaylist(trackId, isRemote)
                    loadCurrentTrack()
                } catch (e: Exception) {
                    _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun loadCurrentTrack() {
            viewModelScope.launch {
                try {
                    playbackJob?.cancel()

                    currentTrack = playlistManager.getCurrentTrack()
                    currentTrack?.let { track ->
                        mediaPlayer.prepare(track.uri)
                        _uiState.value = MusicPlayerUiState.Playing(track = track)
                        observePlaybackState()
                    }
                } catch (e: Exception) {
                    _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        fun handleIntent(intent: MusicPlayerIntent) {
            when (intent) {
                is MusicPlayerIntent.PlayPause -> togglePlayPause()
                is MusicPlayerIntent.SeekTo -> seekTo(intent.position)
                is MusicPlayerIntent.Next -> playNextTrack()
                is MusicPlayerIntent.Previous -> playPreviousTrack()
            }
        }

        private fun playNextTrack() {
            viewModelScope.launch {
                try {
                    playlistManager.getNextTrack()?.let {
                        loadCurrentTrack()
                    }
                } catch (e: Exception) {
                    _uiState.value = MusicPlayerUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun playPreviousTrack() {
            viewModelScope.launch {
                try {
                    playlistManager.getPreviousTrack()?.let {
                        loadCurrentTrack()
                    }
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
