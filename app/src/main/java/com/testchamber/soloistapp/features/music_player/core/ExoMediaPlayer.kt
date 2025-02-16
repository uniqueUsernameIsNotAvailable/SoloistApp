package com.testchamber.soloistapp.features.music_player.core

import android.content.Context
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ExoMediaPlayer
    @Inject
    constructor(
        private val context: Context,
    ) : MediaPlayer {
        private var player: ExoPlayer? = null
        private val _playbackState = MutableStateFlow(PlaybackState())
        override val playbackState: Flow<PlaybackState> = _playbackState.asStateFlow()

        private var updateJob: Job? = null

        private fun createPlayerIfNeeded() {
            if (player == null) {
                player =
                    ExoPlayer.Builder(context).build().apply {
                        addListener(playerListener)
                    }
            }
        }

        private val playerListener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    updatePlaybackState()
                    when (state) {
                        Player.STATE_READY -> {
                            startProgressUpdates()
                        }

                        Player.STATE_ENDED -> {
                            stopProgressUpdates()
                        }

                        Player.STATE_BUFFERING -> {
                        }

                        Player.STATE_IDLE -> {
                            stopProgressUpdates()
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoMediaPlayer", "Player error: ${error.message}", error)
                    _playbackState.value = PlaybackState()
                    stopProgressUpdates()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlaybackState()
                    if (isPlaying) {
                        startProgressUpdates()
                    } else {
                        stopProgressUpdates()
                    }
                }
            }

        private fun startProgressUpdates() {
            updateJob?.cancel()
            updateJob =
                CoroutineScope(Dispatchers.Main).launch {
                    while (isActive) {
                        updatePlaybackState()
                        delay(500)
                    }
                }
        }

        private fun stopProgressUpdates() {
            updateJob?.cancel()
            updateJob = null
        }

        override fun prepare(uri: String) {
            try {
                stopProgressUpdates()
                player?.stop()
                player?.clearMediaItems()
                createPlayerIfNeeded()

                player?.let { exoPlayer ->
                    val mediaItem = MediaItem.fromUri(uri)
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()

                    updatePlaybackState()

                    exoPlayer.playWhenReady = true
                }
            } catch (e: Exception) {
                Log.e("ExoMediaPlayer", "Error preparing track", e)
                _playbackState.value = PlaybackState()
            }
        }

        override fun play() {
            player?.play()
        }

        override fun pause() {
            player?.pause()
        }

        override fun seekTo(position: Long) {
            player?.let { exoPlayer ->
                val duration = exoPlayer.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: 0L
                val validPosition = position.coerceIn(0L, duration)
                exoPlayer.seekTo(validPosition)
            }
        }

        override fun isPlaying(): Boolean = player?.isPlaying ?: false

        override fun release() {
            stopProgressUpdates()
            player?.let { exoPlayer ->
                exoPlayer.removeListener(playerListener)
                exoPlayer.release()
            }
            player = null
            _playbackState.value = PlaybackState()
        }

        private fun updatePlaybackState() {
            player?.let { exoPlayer ->
                val duration =
                    exoPlayer.duration.takeIf { it != C.TIME_UNSET && it > 0 }
                        ?: _playbackState.value.duration
                        ?: 0L

                val currentPosition = exoPlayer.currentPosition.coerceIn(0L, duration)
                val bufferedPosition =
                    exoPlayer.bufferedPosition
                        .takeIf { it != C.TIME_UNSET }
                        ?.coerceIn(0L, duration) ?: currentPosition

                val newState =
                    PlaybackState(
                        isPlaying = exoPlayer.isPlaying,
                        currentPosition = currentPosition,
                        bufferedPosition = bufferedPosition,
                        duration = duration,
                    )

                if (newState != _playbackState.value) {
                    _playbackState.value = newState
                }
            }
        }
    }
