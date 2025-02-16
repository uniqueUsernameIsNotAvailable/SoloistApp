package com.testchamber.soloistapp.core.di

import android.content.Context
import com.testchamber.soloistapp.data.repository.MediaRepositoryImpl
import com.testchamber.soloistapp.data.repository.remote.RemoteMediaRepositoryImpl
import com.testchamber.soloistapp.data.repository.remote.DeezerApi
import com.testchamber.soloistapp.domain.repository.MediaRepository
import com.testchamber.soloistapp.domain.repository.remote.RemoteMediaRepository
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideMediaRepository(context: Context): MediaRepository = MediaRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideRemoteMediaRepository(deezerApi: DeezerApi): RemoteMediaRepository = RemoteMediaRepositoryImpl(deezerApi)
}
