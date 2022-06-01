package ariesvelasquez.com.republikapc.di.initializer

import android.app.Application
import ariesvelasquez.com.republikapc.di.AppInitializer
import javax.inject.Inject

class AppInitializers @Inject constructor(
    private val initializers: Set<@JvmSuppressWildcards AppInitializer>
){
    fun initialize(application: Application) {
        initializers.forEach {
            it.initialize(application)
        }
    }
}