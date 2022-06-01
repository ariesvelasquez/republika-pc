package ariesvelasquez.com.republikapc.di.module

import android.app.Application
import ariesvelasquez.com.republikapc.datasource.local.RepublikaPCDatabase
import ariesvelasquez.com.republikapc.datasource.local.SellerCachedKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesRoomDatabase(app: Application): RepublikaPCDatabase {
        return RepublikaPCDatabase.create(app)
    }
}