package com.testchamber.soloistapp.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import jakarta.inject.Inject
import jakarta.inject.Provider
import kotlin.reflect.KClass

class ViewModelFactory
    @Inject
    constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val creator =
                creators[modelClass] ?: creators.entries
                    .firstOrNull {
                        modelClass.isAssignableFrom(it.key)
                    }?.value ?: throw IllegalArgumentException("Unknown model class $modelClass")
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        }
    }

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(
    val value: KClass<out ViewModel>,
)
