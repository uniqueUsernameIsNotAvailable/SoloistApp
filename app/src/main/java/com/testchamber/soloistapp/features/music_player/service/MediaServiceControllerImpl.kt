package com.testchamber.soloistapp.features.music_player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.testchamber.soloistapp.R
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaybackState
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import com.testchamber.soloistapp.features.music_player.service.MediaService.Companion.NOTIFICATION_ID
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MediaServiceControllerImpl
    @Inject
    constructor(
        private val context: Context,
        private val mediaPlayer: MediaPlayer,
        private val playlistManager: PlaylistManager,
    ) : MediaServiceController {
        private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        private val mediaSession = MediaSessionCompat(context, "MusicPlayerSession")

        private val _currentTrack = MutableStateFlow<Track?>(null)
        override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

        private var playbackJob: Job? = null
        private var currentPlaybackState: PlaybackState = PlaybackState()

        override val playbackState: Flow<PlaybackState> =
            mediaPlayer.playbackState
                .onEach { state ->
                    currentPlaybackState = state
                }.shareIn(controllerScope, SharingStarted.Eagerly, 1)

        companion object {
            private const val NOTIFICATION_CHANNEL_ID = "MediaPlayerChannel"
            private const val NOTIFICATION_ID = 1
        }

        init {
            createNotificationChannel()
            initializeMediaSession()
            observePlaybackState()
        }

        private fun observePlaybackState() {
            playbackJob?.cancel() // Cancel any existing job
            playbackJob =
                controllerScope.launch {
                    mediaPlayer.playbackState
                        .collect { state ->
                            currentPlaybackState = state
                            updateMediaSession(state)
                            updateNotification()
                        }
                }
        }

        private fun initializeMediaSession() {
            mediaSession.setCallback(
                object : MediaSessionCompat.Callback() {
                    override fun onSeekTo(pos: Long) {
                        mediaPlayer.seekTo(pos)

                        controllerScope.launch {
                            updateMediaSession(currentPlaybackState)
                            updateNotification()
                        }
                    }

                    override fun onPlay() {
                        mediaPlayer.play()
                    }

                    override fun onPause() {
                        mediaPlayer.pause()
                    }

                    override fun onSkipToNext() {
                        next()
                    }

                    override fun onSkipToPrevious() {
                        previous()
                    }
                },
            )
            mediaSession.isActive = true
        }

        override fun playTrack(track: Track) {
            controllerScope.launch {
                try {
                    _currentTrack.value = track
                    mediaPlayer.prepare(track.uri)
                    mediaPlayer.play()

                    observePlaybackState()

                    updateNotification()
                } catch (e: Exception) {
                    Log.e("MediaController", "Error playing track", e)
                }
            }
        }

        private fun updateMediaSession(state: PlaybackState) {
            try {
                val currentPosition = state.currentPosition.takeIf { it >= 0 } ?: 0L
                val duration =
                    state.duration.takeIf { it > 0 }
                        ?: _currentTrack.value?.duration
                        ?: 0L

                val playbackState =
                    PlaybackStateCompat
                        .Builder()
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_SEEK_TO,
                        ).setState(
                            if (state.isPlaying) {
                                PlaybackStateCompat.STATE_PLAYING
                            } else {
                                PlaybackStateCompat.STATE_PAUSED
                            },
                            currentPosition,
                            1.0f,
                        ).build()

                mediaSession.setPlaybackState(playbackState)

                _currentTrack.value?.let { track ->
                    val metadata =
                        MediaMetadataCompat
                            .Builder()
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                            .build()
                    mediaSession.setMetadata(metadata)
                    mediaSession.isActive
                }
            } catch (e: Exception) {
                Log.e("MediaController", "Error updating media session", e)
            }
        }

        override fun togglePlayPause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.play()
            }
            updateNotification()
        }

        override fun next() {
            controllerScope.launch {
                try {
                    val nextTrack = playlistManager.getNextTrack()
                    if (nextTrack == null) {
                        Log.e("MediaController", "Failed to get next track")
                        return@launch
                    }

                    _currentTrack.value = nextTrack
                    mediaPlayer.prepare(nextTrack.uri)
                    mediaPlayer.play()
                    updateNotification()
                } catch (e: Exception) {
                    Log.e("MediaController", "Error playing next track", e)
                }
            }
        }

        override fun previous() {
            controllerScope.launch {
                try {
                    val previousTrack = playlistManager.getPreviousTrack()
                    if (previousTrack == null) {
                        Log.e("MediaController", "Failed to get previous track")
                        return@launch
                    }

                    _currentTrack.value = previousTrack
                    mediaPlayer.prepare(previousTrack.uri)
                    mediaPlayer.play()
                    updateNotification()
                } catch (e: Exception) {
                    Log.e("MediaController", "Error playing previous track", e)
                }
            }
        }

        override fun seekTo(position: Long) {
            mediaPlayer.seekTo(position)
        }

        private fun formatDuration(durationMs: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                    TimeUnit.MINUTES.toSeconds(minutes)
            return String.format("%02d:%02d", minutes, seconds)
        }

        override fun release() {
            controllerScope.launch {
                try {
                    playbackJob?.cancel()
                    mediaPlayer.release()
                    mediaSession.release()
                    _currentTrack.value = null
                    notificationManager.cancel(NOTIFICATION_ID)
                } catch (e: Exception) {
                    Log.e("MediaController", "Error releasing media controller", e)
                }
            }
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Media Player",
                        NotificationManager.IMPORTANCE_LOW,
                    ).apply {
                        description = "Media player controls"
                        setShowBadge(false)
                    }
                notificationManager.createNotificationChannel(channel)
            }
        }

        override fun updateNotification() {
            val track = _currentTrack.value ?: return
            val notification = createNotification(track, currentPlaybackState)
            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e("MediaController", "Error updating notification", e)
            }
        }

        private fun createNotification(
            track: Track,
            state: PlaybackState,
        ): Notification {
            val mediaStyle =
                androidx.media.app.NotificationCompat
                    .MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)

            val currentPosition = state.currentPosition.takeIf { it > 0 } ?: 0L
            val duration = state.duration.takeIf { it > 0 } ?: track.duration

            val builder =
                NotificationCompat
                    .Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note_24dp)
                    .setContentTitle(track.title)
                    .setContentText("Artist - ${track.artist}")
                    .setSubText("${formatDuration(currentPosition)} / ${formatDuration(duration)}")
                    .setOngoing(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setStyle(mediaStyle)
                    .setProgress(
                        duration.toInt(),
                        currentPosition.toInt(),
                        state.bufferedPosition == state.currentPosition,
                    )

            // Previous
            val previousIntent =
                Intent(context, MediaReceiver::class.java)
                    .setAction(MediaReceiver.ACTION_PREVIOUS)
            val previousPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    previousIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent)

            // Play/Pause
            val playPauseIntent =
                Intent(context, MediaReceiver::class.java)
                    .setAction(MediaReceiver.ACTION_PLAY_PAUSE)
            val playPausePendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    1,
                    playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.addAction(
                if (state.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (state.isPlaying) "Pause" else "Play",
                playPausePendingIntent,
            )

            // Next
            val nextIntent =
                Intent(context, MediaReceiver::class.java)
                    .setAction(MediaReceiver.ACTION_NEXT)
            val nextPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    2,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)

            return builder.build()
        }
    }
