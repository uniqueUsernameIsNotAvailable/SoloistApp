package com.testchamber.soloistapp.core.di

import android.content.Context
import com.testchamber.soloistapp.domain.repository.MediaRepository
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import com.testchamber.soloistapp.features.music_player.service.MediaServiceController
import com.testchamber.soloistapp.features.music_player.service.MediaServiceControllerImpl
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton

@Module
class ServiceModule {
    @Provides
    @Singleton
    fun provideMediaServiceController(
        context: Context,
        mediaPlayer: MediaPlayer,
        playlistManager: PlaylistManager,
    ): MediaServiceController = MediaServiceControllerImpl(context, mediaPlayer, playlistManager)

    @Provides
    @Singleton
    fun providePlaylistManager(
        mediaRepository: MediaRepository,
        remoteMediaRepository: RemoteMediaRepository,
    ): PlaylistManager = PlaylistManager(mediaRepository, remoteMediaRepository)
}
