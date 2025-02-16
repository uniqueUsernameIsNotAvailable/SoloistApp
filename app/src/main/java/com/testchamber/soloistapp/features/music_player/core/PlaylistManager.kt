package com.testchamber.soloistapp.features.music_player.core

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.MediaRepository
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject

class PlaylistManager
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
        private val remoteMediaRepository: RemoteMediaRepository,
    ) {
        private var currentPlaylist: List<Track> = emptyList()
        private var currentIndex: Int = -1
        private var isRemotePlaylist: Boolean = false

        suspend fun initializePlaylist(
            trackId: String,
            isRemote: Boolean,
        ) {
            isRemotePlaylist = isRemote
            currentPlaylist =
                if (isRemote) {
                    remoteMediaRepository.getTopTracks()
                } else {
                    mediaRepository.getLocalTracks()
                }
            currentIndex = currentPlaylist.indexOfFirst { it.id == trackId }
        }

        fun getCurrentTrack(): Track? = if (currentIndex in currentPlaylist.indices) currentPlaylist[currentIndex] else null

        fun getNextTrack(): Track? {
            if (currentPlaylist.isEmpty()) return null
            currentIndex = (currentIndex + 1) % currentPlaylist.size
            return getCurrentTrack()
        }

        fun getPreviousTrack(): Track? {
            if (currentPlaylist.isEmpty()) return null
            currentIndex = if (currentIndex <= 0) currentPlaylist.size - 1 else currentIndex - 1
            return getCurrentTrack()
        }

        fun isRemote() = isRemotePlaylist
    }
