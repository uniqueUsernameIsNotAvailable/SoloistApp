package com.testchamber.soloistapp.features.music_player.core

import android.content.Context
import androidx.media3.common.MediaItem
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
        private val player: ExoPlayer = ExoPlayer.Builder(context).build()
        private val _playbackState = MutableStateFlow(PlaybackState())
        override val playbackState: Flow<PlaybackState> = _playbackState.asStateFlow()

        private val updateJob =
            CoroutineScope(Dispatchers.Main + Job()).launch {
                while (isActive) {
                    _playbackState.value =
                        PlaybackState(
                            isPlaying = player.isPlaying,
                            currentPosition = player.currentPosition,
                            bufferedPosition = player.bufferedPosition,
                            duration = player.duration,
                        )
                    delay(16)
                }
            }

        init {
            player.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        updatePlaybackState()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updatePlaybackState()
                    }
                },
            )
        }

        override fun prepare(uri: String) {
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
        }

        override fun play() {
            player.play()
        }

        override fun pause() {
            player.pause()
        }

        override fun seekTo(position: Long) {
            player.seekTo(position)
        }

        override fun isPlaying(): Boolean = player.isPlaying

        override fun release() {
            updateJob.cancel()
            player.release()
        }

        private fun updatePlaybackState() {
            _playbackState.value =
                PlaybackState(
                    isPlaying = player.isPlaying,
                    currentPosition = player.currentPosition,
                    bufferedPosition = player.bufferedPosition,
                    duration = player.duration,
                )
        }
    }
