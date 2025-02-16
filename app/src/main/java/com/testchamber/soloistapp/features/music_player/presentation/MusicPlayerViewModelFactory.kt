package com.testchamber.soloistapp.features.music_player.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.App
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import jakarta.inject.Inject

class MusicPlayerViewModelFactory
    @Inject
    constructor(
        private val getTrackUseCase: GetTrackUseCase,
        private val mediaPlayer: MediaPlayer,
        private val playlistManager: PlaylistManager,
        private val trackId: String,
        private val isRemote: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
                return MusicPlayerViewModel(
                    getTrackUseCase = getTrackUseCase,
                    mediaPlayer = mediaPlayer,
                    playlistManager = playlistManager,
                    trackId = trackId,
                    isRemote = isRemote,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

@Composable
fun provideMusicPlayerViewModelFactory(
    trackId: String,
    isRemote: Boolean,
    context: Context = LocalContext.current,
): ViewModelProvider.Factory {
    val app = context.applicationContext as App
    return MusicPlayerViewModelFactory(
        getTrackUseCase = app.appComponent.getTrackUseCase(),
        mediaPlayer = app.appComponent.mediaPlayer(),
        playlistManager = app.appComponent.playlistManager(),
        trackId = trackId,
        isRemote = isRemote,
    )
}
