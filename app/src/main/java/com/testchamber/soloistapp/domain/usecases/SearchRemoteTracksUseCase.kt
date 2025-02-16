package com.testchamber.soloistapp.domain.usecases

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject

class SearchRemoteTracksUseCase
    @Inject
    constructor(
        private val remoteMediaRepository: RemoteMediaRepository,
    ) {
        suspend fun execute(query: String): List<Track> = remoteMediaRepository.searchTracks(query)
    }
