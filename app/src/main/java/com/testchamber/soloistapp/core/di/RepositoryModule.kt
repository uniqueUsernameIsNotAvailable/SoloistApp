package com.testchamber.soloistapp.core.di

import android.content.Context
import com.testchamber.soloistapp.data.repository.MediaRepositoryImpl
import com.testchamber.soloistapp.domain.repository.MediaRepository
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideMediaRepository(context: Context): MediaRepository = MediaRepositoryImpl(context)
}
