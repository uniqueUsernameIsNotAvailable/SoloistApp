package com.testchamber.soloistapp.domain.usecases

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.MediaRepository
import jakarta.inject.Inject

class GetLocalTracksUseCase
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
    ) {
        suspend fun execute(): List<Track> = mediaRepository.getLocalTracks()
    }
