package com.testchamber.soloistapp.data.repository.remote

import com.testchamber.soloistapp.domain.models.RemoteChartResponse
import com.testchamber.soloistapp.domain.models.RemoteSearchResponse
import com.testchamber.soloistapp.domain.models.RemoteTrack
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {
    @GET("chart")
    suspend fun getTopTracks(): RemoteChartResponse

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
    ): RemoteSearchResponse

    @GET("track/{id}")
    suspend fun getTrack(
        @Path("id") id: String,
    ): RemoteTrack
}
