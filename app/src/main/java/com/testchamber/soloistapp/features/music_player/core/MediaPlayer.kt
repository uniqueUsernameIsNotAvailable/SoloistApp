package com.testchamber.soloistapp.features.music_player.core

import kotlinx.coroutines.flow.Flow

interface MediaPlayer {
    val playbackState: Flow<PlaybackState>

    fun prepare(uri: String)

    fun play()

    fun pause()

    fun seekTo(position: Long)

    fun isPlaying(): Boolean

    fun release()
}

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val duration: Long = 0L,
) {
    val isValid: Boolean
        get() = duration > 0
}
