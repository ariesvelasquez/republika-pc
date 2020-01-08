package ariesvelasquez.com.republikapc.repository.dashboard

import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.Listing
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference

interface IDashboardRepository {

    fun user(userID: String): DocumentReference

    fun feeds(): Listing<FeedItem>

    fun sellerItems(sellerName: String) : Listing<FeedItem>

    fun rigs(): Listing<Rig>

    fun saved(): Listing<Saved>

    fun saveItem(firebaseUser: FirebaseUser, feedItem: FeedItem): Task<Void>

    fun deleteRig(firebaseUser: FirebaseUser, rigId: String): Task<Void>

    fun deleteSaved(firebaseUser: FirebaseUser, savedId: String): Task<Void>

    fun rigItems(rigId: String) : Listing<FeedItem>

    fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void>

    fun searchFeeds(searchVal: String): Listing<FeedItem>

    fun addItemToRig(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: FeedItem
    ) : Task<Void>

    fun addSavedItemToRig(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: Saved
    ) : Task<Void>

    fun deleteRigItem(rigId: String, rigItemId: String): Task<Void>
}