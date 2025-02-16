package com.testchamber.soloistapp.features.music_player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import com.testchamber.soloistapp.features.music_player.service.MediaServiceController
import jakarta.inject.Inject

class MusicPlayerViewModelFactory(
    private val getTrackUseCase: GetTrackUseCase,
    private val mediaController: MediaServiceController,
    private val playlistManager: PlaylistManager,
    private val trackId: String,
    private val isRemote: Boolean,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            return MusicPlayerViewModel(
                getTrackUseCase = getTrackUseCase,
                mediaController = mediaController,
                playlistManager = playlistManager,
                trackId = trackId,
                isRemote = isRemote,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    class Provider
        @Inject
        constructor(
            private val getTrackUseCase: GetTrackUseCase,
            private val mediaController: MediaServiceController,
            private val playlistManager: PlaylistManager,
        ) {
            fun create(
                trackId: String,
                isRemote: Boolean,
            ): MusicPlayerViewModelFactory =
                MusicPlayerViewModelFactory(
                    getTrackUseCase = getTrackUseCase,
                    mediaController = mediaController,
                    playlistManager = playlistManager,
                    trackId = trackId,
                    isRemote = isRemote,
                )
        }
}
