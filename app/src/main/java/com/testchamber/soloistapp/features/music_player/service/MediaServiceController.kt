package com.testchamber.soloistapp.features.music_player.service

import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.features.music_player.core.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaServiceController {
    val playbackState: Flow<PlaybackState>
    val currentTrack: StateFlow<Track?>

    fun playTrack(track: Track)

    fun togglePlayPause()

    fun next()

    fun previous()

    fun seekTo(position: Long)

    fun updateNotification()

    fun release()
}
