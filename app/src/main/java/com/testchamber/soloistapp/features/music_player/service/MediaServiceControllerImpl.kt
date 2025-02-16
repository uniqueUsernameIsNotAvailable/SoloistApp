package com.testchamber.soloistapp.features.music_player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.testchamber.soloistapp.R
import com.testchamber.soloistapp.domain.models.Track
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaybackState
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

        private val _currentTrack = MutableStateFlow<Track?>(null)
        override val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

        private var playbackJob: Job? = null

        override val playbackState: Flow<PlaybackState> = mediaPlayer.playbackState

        companion object {
            private const val NOTIFICATION_CHANNEL_ID = "MediaPlayerChannel"
            private const val NOTIFICATION_ID = 1
        }

        init {
            createNotificationChannel()
            observePlaybackState()
        }

        private fun observePlaybackState() {
            controllerScope.launch {
                playbackState.collect { state ->
                    updateNotification()
                }
            }
        }

        override fun playTrack(track: Track) {
            controllerScope.launch {
                try {
                    _currentTrack.value = track

                    playbackJob?.cancel()

                    mediaPlayer.prepare(track.uri)
                    mediaPlayer.play()
                    updateNotification()

                    playbackJob =
                        launch {
                            playbackState.collect { state ->
                                updateNotification()
                            }
                        }
                } catch (e: Exception) {
                    Log.e("MediaController", "Error playing track", e)
                }
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

        override fun release() {
            controllerScope.launch {
                try {
                    playbackJob?.cancel()
                    mediaPlayer.release()
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
            val notification = createNotification(track)
            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e("MediaController", "Error updating notification", e)
            }
        }

        private fun createNotification(track: Track): Notification {
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val playPauseIntent =
                Intent(context, MediaReceiver::class.java).apply {
                    action = MediaReceiver.ACTION_PLAY_PAUSE
                }
            val nextIntent =
                Intent(context, MediaReceiver::class.java).apply {
                    action = MediaReceiver.ACTION_NEXT
                }
            val previousIntent =
                Intent(context, MediaReceiver::class.java).apply {
                    action = MediaReceiver.ACTION_PREVIOUS
                }

            val playPausePendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    0,
                    playPauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val nextPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    1,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val previousPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    2,
                    previousIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            return NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_24dp)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .addAction(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    previousPendingIntent,
                ).addAction(
                    if (mediaPlayer.isPlaying()) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (mediaPlayer.isPlaying()) "Pause" else "Play",
                    playPausePendingIntent,
                ).addAction(
                    android.R.drawable.ic_media_next,
                    "Next",
                    nextPendingIntent,
                ).setStyle(
                    androidx.media.app.NotificationCompat
                        .MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2),
                ).build()
        }
    }
