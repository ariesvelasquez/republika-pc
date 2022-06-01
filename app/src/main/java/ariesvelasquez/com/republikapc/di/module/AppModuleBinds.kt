package ariesvelasquez.com.republikapc.di.module

import androidx.paging.ExperimentalPagingApi
import ariesvelasquez.com.republikapc.di.AppInitializer
import ariesvelasquez.com.republikapc.di.initializer.firebase.FirestoreDatabaseInitializer
import ariesvelasquez.com.republikapc.di.initializer.timber.TimberInitializer
import ariesvelasquez.com.republikapc.repository.dashboard.IRPCRepository
import ariesvelasquez.com.republikapc.repository.dashboard.RPCRepository
import ariesvelasquez.com.republikapc.repository.seller.ISellerRepository
import ariesvelasquez.com.republikapc.repository.seller.SellerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModuleBinds {

    @Binds
    @IntoSet
    abstract fun provideTimberInitializer(timberInitializer: TimberInitializer) : AppInitializer

    @Binds
    @IntoSet
    abstract fun provideFirestoreInitializer(firestoreInitializer: FirestoreDatabaseInitializer) : AppInitializer

    @Binds
    abstract fun provideRPCRepository(impl: RPCRepository): IRPCRepository

    @Binds
    abstract fun provideSellerRepository(impl: SellerRepository): ISellerRepository
}