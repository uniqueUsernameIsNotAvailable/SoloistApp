package com.testchamber.soloistapp.domain.repository.remote

import com.testchamber.soloistapp.domain.models.Track

interface RemoteMediaRepository {
    suspend fun getTopTracks(): List<Track>

    suspend fun searchTracks(query: String): List<Track>

    suspend fun getTrack(id: String): Track
}
