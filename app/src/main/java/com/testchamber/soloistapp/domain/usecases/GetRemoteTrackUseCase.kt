package com.testchamber.soloistapp.domain.usecases

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject

class GetRemoteTrackUseCase
    @Inject
    constructor(
        private val remoteMediaRepository: RemoteMediaRepository,
    ) {
        suspend fun execute(id: String): Track = remoteMediaRepository.getTrack(id)
    }
