package com.testchamber.soloistapp.core.di

import androidx.lifecycle.ViewModel
import com.testchamber.soloistapp.features.music_player.presentation.MusicPlayerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SavedStateHandleModule {
    @Binds
    @IntoMap
    @ViewModelKey(MusicPlayerViewModel::class)
    abstract fun bindMusicPlayerViewModel(factory: MusicPlayerViewModel.Factory): AssistedSavedStateViewModelFactory<out ViewModel>
}
