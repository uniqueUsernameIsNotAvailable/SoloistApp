package com.testchamber.soloistapp.core.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import com.testchamber.soloistapp.features.music_player.core.PlaylistManager
import com.testchamber.soloistapp.features.music_player.presentation.MusicPlayerViewModelFactory
import com.testchamber.soloistapp.features.music_player.service.MediaService
import com.testchamber.soloistapp.features.music_player.service.MediaServiceController
import dagger.BindsInstance
import dagger.Component
import jakarta.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class,
        RepositoryModule::class,
        ServiceModule::class,
    ],
)
interface AppComponent {
    fun viewModelFactory(): ViewModelProvider.Factory

    fun mediaPlayer(): MediaPlayer

    fun inject(service: MediaService)

    fun playlistManager(): PlaylistManager

    fun getTrackUseCase(): GetTrackUseCase

    fun musicPlayerViewModelFactoryProvider(): MusicPlayerViewModelFactory.Provider

    fun mediaServiceController(): MediaServiceController

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
        ): AppComponent
    }
}
