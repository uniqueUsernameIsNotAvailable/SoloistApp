package com.testchamber.soloistapp.core.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import jakarta.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelModule::class,
        SavedStateViewModelModule::class,
        RepositoryModule::class,
    ],
)
interface AppComponent {
    fun viewModelFactory(): ViewModelProvider.Factory

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance context: Context,
            @BindsInstance application: Application,
        ): AppComponent
    }
}
