package com.testchamber.soloistapp.core

import androidx.lifecycle.ViewModelProvider

interface ComponentProvider {
    fun provideViewModelFactory(): ViewModelProvider.Factory
}
