package ariesvelasquez.com.republikapc.repository.dashboard

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.Listing
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser

interface IDashboardRepository {
    fun feeds(): Listing<FeedItem>

    fun rigs(): Listing<Rig>

    fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void>
}