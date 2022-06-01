package ariesvelasquez.com.republikapc

import android.app.Application
import ariesvelasquez.com.republikapc.di.initializer.AppInitializers
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RepublikaPC : Application() {

    @Inject
    lateinit var appInitializers: AppInitializers

    companion object {
        var globalFlags = GlobalFlags()
    }

    override fun onCreate() {
        super.onCreate()

        appInitializers.initialize(this)
    }
}