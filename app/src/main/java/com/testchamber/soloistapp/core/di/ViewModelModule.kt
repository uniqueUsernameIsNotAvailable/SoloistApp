package com.testchamber.soloistapp.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.features.local_music.presentation.LocalMusicViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(LocalMusicViewModel::class)
    abstract fun bindLocalMusicViewModel(viewModel: LocalMusicViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
