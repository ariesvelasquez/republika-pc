package ariesvelasquez.com.republikapc.di.initializer.timber

import android.app.Application
import ariesvelasquez.com.republikapc.BuildConfig
import ariesvelasquez.com.republikapc.di.AppInitializer
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

class TimberInitializer @Inject constructor() : AppInitializer {

    override fun initialize(application: Application) {
        // Setup Timber Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Fabric.with(application, Crashlytics())
            Timber.plant(CrashReportingTree())
        }
    }
}