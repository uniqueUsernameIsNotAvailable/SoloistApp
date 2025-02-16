package com.testchamber.soloistapp.data.repository.remote

import com.testchamber.soloistapp.domain.models.RemoteTrack
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import jakarta.inject.Inject

class RemoteMediaRepositoryImpl
    @Inject
    constructor(
        private val deezerApi: DeezerApi,
    ) : RemoteMediaRepository {
        override suspend fun getTopTracks(): List<Track> =
            deezerApi
                .getTopTracks()
                .tracks.data
                .map { it.toTrack() }

        override suspend fun searchTracks(query: String): List<Track> = deezerApi.searchTracks(query).data.map { it.toTrack() }

        override suspend fun getTrack(id: String): Track = deezerApi.getTrack(id).toTrack()

        private fun RemoteTrack.toTrack() =
            Track(
                id = id,
                title = title,
                artist = artist.name,
                duration = duration * 1000L,
                uri = preview,
                coverArtUri = album.coverMedium,
            )
    }
