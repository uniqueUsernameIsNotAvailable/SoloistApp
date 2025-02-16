package com.testchamber.soloistapp.core.di

import androidx.lifecycle.ViewModel
import com.testchamber.soloistapp.features.music_player.presentation.MusicPlayerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SavedStateViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MusicPlayerViewModel::class)
    abstract fun bindMusicPlayerViewModelFactory(factory: MusicPlayerViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>
}
