package com.testchamber.soloistapp.core.di

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jakarta.inject.Inject
import jakarta.inject.Provider

class SavedStateViewModelFactory
    @Inject
    constructor(
        private val assistedFactories: Map<Class<out ViewModel>, @JvmSuppressWildcards AssistedSavedStateViewModelFactory<out ViewModel>>,
        private val defaultFactories: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
        private val application: Application,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // ViewModel w SavedStateHandle
            val assistedFactory =
                assistedFactories[modelClass]
                    ?: assistedFactories.entries
                        .firstOrNull {
                            modelClass.isAssignableFrom(it.key)
                        }?.value

            if (assistedFactory != null) {
                @Suppress("UNCHECKED_CAST")
                return assistedFactory.create(createSavedStateHandle()) as T
            }

            // fail? -> regular VM
            val creator =
                defaultFactories[modelClass]
                    ?: defaultFactories.entries
                        .firstOrNull {
                            modelClass.isAssignableFrom(it.key)
                        }?.value
                    ?: throw IllegalArgumentException("Unknown model class $modelClass")

            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        }

        private fun createSavedStateHandle(): SavedStateHandle = SavedStateHandle()
    }
