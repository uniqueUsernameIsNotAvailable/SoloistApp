package com.testchamber.soloistapp.domain.usecases

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.MediaRepository
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject

class GetTrackUseCase
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
        private val remoteMediaRepository: RemoteMediaRepository,
    ) {
        suspend fun execute(
            trackId: String,
            isRemote: Boolean,
        ): Track =
            if (isRemote) {
                remoteMediaRepository.getTrack(trackId)
            } else {
                mediaRepository.getLocalTracks().find { it.id == trackId }
                    ?: throw IllegalArgumentException("Track not found")
            }
    }
