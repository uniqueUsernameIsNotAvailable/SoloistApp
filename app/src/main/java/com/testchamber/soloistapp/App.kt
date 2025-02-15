package com.testchamber.soloistapp

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.testchamber.soloistapp.core.ComponentProvider
import com.testchamber.soloistapp.core.di.AppComponent
import com.testchamber.soloistapp.core.di.DaggerAppComponent

class App :
    Application(),
    ComponentProvider {
    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }

    override fun provideViewModelFactory(): ViewModelProvider.Factory = appComponent.viewModelFactory()
}
