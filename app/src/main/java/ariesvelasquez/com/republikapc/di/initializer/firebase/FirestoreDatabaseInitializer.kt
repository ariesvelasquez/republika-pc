package ariesvelasquez.com.republikapc.di.initializer.firebase

import android.app.Application
import ariesvelasquez.com.republikapc.di.AppInitializer
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestoreSettings

class FirestoreDatabaseInitializer @Inject constructor() : AppInitializer {

    override fun initialize(application: Application) {
        FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
    }
}