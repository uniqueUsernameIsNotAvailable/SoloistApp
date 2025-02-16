package com.testchamber.soloistapp.core.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.domain.usecases.GetTrackUseCase
import com.testchamber.soloistapp.features.music_player.core.MediaPlayer
import dagger.BindsInstance
import dagger.Component
import jakarta.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class,
        RepositoryModule::class,
    ],
)
interface AppComponent {
    fun viewModelFactory(): ViewModelProvider.Factory

    fun getTrackUseCase(): GetTrackUseCase

    fun mediaPlayer(): MediaPlayer

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
        ): AppComponent
    }
}
