package ariesvelasquez.com.republikapc.di

import android.app.Application

interface AppInitializer {
    fun initialize(application: Application)
}