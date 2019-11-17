/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ariesvelasquez.com.republikapc.utils

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import ariesvelasquez.com.republikapc.Const.RIGS_COLLECTION
import ariesvelasquez.com.republikapc.Const.USERS_COLLECTION
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.db.TipidPCDatabase
import ariesvelasquez.com.republikapc.repository.auth.AuthRepository
import ariesvelasquez.com.republikapc.repository.dashboard.DashboardRepository
import ariesvelasquez.com.republikapc.repository.dashboard.IDashboardRepository
import ariesvelasquez.com.republikapc.repository.search.ISearchRepository
import ariesvelasquez.com.republikapc.repository.search.SearchRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                        app = context.applicationContext as Application
                    )
                }
                return instance!!
            }
        }

        /**
         * Allows tests to replace the default implementations.
         */
        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getDashboardRepository(): IDashboardRepository

    fun getSearchRepository() : ISearchRepository

    fun getAuthRepository(): AuthRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getTipidPCApi(): TipidPCApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application) : ServiceLocator {

    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy {
        TipidPCDatabase.create(app)
    }

    private val api by lazy {
        TipidPCApi.create()
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestoreSettings by lazy {
        FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
    }

    private val firestoreRef by lazy {
        val firestoreReference = FirebaseFirestore.getInstance()
        firestoreReference.firestoreSettings = firestoreSettings
        firestoreReference
    }


    override fun getDashboardRepository(): IDashboardRepository {
        return DashboardRepository(
            db = db,
            firestore = firestoreRef,
            tipidPCApi = getTipidPCApi(),
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getSearchRepository(): ISearchRepository {
        return SearchRepository(
            tipidPCApi = getTipidPCApi(),
            networkExecutor = getNetworkExecutor()
        )
    }

    override fun getAuthRepository(): AuthRepository {
        return AuthRepository(
            firebaseAuth,
            firestoreRef.collection(USERS_COLLECTION)
        )
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getTipidPCApi(): TipidPCApi = api
}

