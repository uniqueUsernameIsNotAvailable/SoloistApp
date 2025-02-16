package com.testchamber.soloistapp.features.music_player.core

import android.util.Log
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.MediaRepository
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class PlaylistManager
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
        private val remoteMediaRepository: RemoteMediaRepository,
    ) {
        private val _playlistState = MutableStateFlow(PlaylistState())

        private val _currentTrack = MutableStateFlow<Track?>(null)

        data class PlaylistState(
            val tracks: List<Track> = emptyList(),
            val currentIndex: Int = -1,
            val isRemote: Boolean = false,
        )

        suspend fun initializePlaylist(
            trackId: String,
            isRemote: Boolean,
        ) {
            try {
                val tracks =
                    if (isRemote) {
                        remoteMediaRepository.getTopTracks()
                    } else {
                        mediaRepository.getLocalTracks()
                    }

                val initialIndex = tracks.indexOfFirst { it.id == trackId }

                _playlistState.value =
                    PlaylistState(
                        tracks = tracks,
                        currentIndex = initialIndex,
                        isRemote = isRemote,
                    )

                updateCurrentTrack()
            } catch (e: Exception) {
                Log.e("PlaylistManager", "Error initializing playlist", e)
                throw e
            }
        }

        private fun updateCurrentTrack() {
            _currentTrack.value = getCurrentTrack()
        }

        fun getCurrentTrack(): Track? {
            val state = _playlistState.value
            return if (state.currentIndex in state.tracks.indices) {
                state.tracks[state.currentIndex]
            } else {
                null
            }
        }

        suspend fun getNextTrack(): Track? {
            val currentState = _playlistState.value
            if (currentState.tracks.isEmpty()) {
                Log.d("PlaylistManager", "Cannot get next track: playlist is empty")
                return null
            }

            val nextIndex = (currentState.currentIndex + 1) % currentState.tracks.size

            _playlistState.value = currentState.copy(currentIndex = nextIndex)
            updateCurrentTrack()

            val track = getCurrentTrack()
            return track
        }

        suspend fun getPreviousTrack(): Track? {
            val currentState = _playlistState.value
            if (currentState.tracks.isEmpty()) {
                Log.d("PlaylistManager", "Cannot get previous track: playlist is empty")
                return null
            }

            val previousIndex =
                if (currentState.currentIndex <= 0) {
                    currentState.tracks.size - 1
                } else {
                    currentState.currentIndex - 1
                }

            _playlistState.value = currentState.copy(currentIndex = previousIndex)
            updateCurrentTrack()

            val track = getCurrentTrack()
            return track
        }

        fun isRemote() = _playlistState.value.isRemote
    }
