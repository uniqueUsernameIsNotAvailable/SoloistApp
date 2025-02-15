package com.testchamber.soloistapp.domain.repository

import com.testchamber.soloistapp.domain.models.Track

interface MediaRepository {
    suspend fun getLocalTracks(): List<Track>
}
