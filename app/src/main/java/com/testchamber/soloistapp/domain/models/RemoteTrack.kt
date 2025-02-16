package com.testchamber.soloistapp.domain.models

import com.google.gson.annotations.SerializedName

data class RemoteTrack(
    val id: String,
    val title: String,
    val artist: RemoteArtist,
    val album: RemoteAlbum,
    val preview: String,
    val duration: Int,
)

data class RemoteArtist(
    val name: String,
)

data class RemoteAlbum(
    @SerializedName("cover_small")
    val coverSmall: String,
    @SerializedName("cover_medium")
    val coverMedium: String,
    @SerializedName("cover")
    val cover: String,
)

data class RemoteChartResponse(
    val tracks: RemoteTrackList,
)

data class RemoteTrackList(
    val data: List<RemoteTrack>,
)

data class RemoteSearchResponse(
    val data: List<RemoteTrack>,
)
