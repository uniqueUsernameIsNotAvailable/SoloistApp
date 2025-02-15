package com.testchamber.soloistapp.domain.models

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: String,
    val coverArtUri: String? = null,
)
