package com.testchamber.soloistapp.core.di

import com.testchamber.soloistapp.data.repository.remote.DeezerApi
import dagger.Module
import dagger.Provides
import jakarta.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideDeezerApi(retrofit: Retrofit): DeezerApi = retrofit.create(DeezerApi::class.java)
}
