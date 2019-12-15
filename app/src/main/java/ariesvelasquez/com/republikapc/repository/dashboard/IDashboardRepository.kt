package ariesvelasquez.com.republikapc.repository.dashboard

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.Listing
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference

interface IDashboardRepository {

    fun user(userID: String): DocumentReference

    fun feeds(): Listing<FeedItem>

    fun rigs(): Listing<Rig>

    fun rigItems(rigId: String) : Listing<FeedItem>

    fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void>

    fun addItemToRig(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: FeedItem
    ) : Task<Void>

    fun deleteRigItem(rigId: String, rigItemId: String): Task<Void>
}