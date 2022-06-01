package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.lifecycle.MutableLiveData
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.Listing
import ariesvelasquez.com.republikapc.repository.NetworkState
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference

interface IDashboardRepository {

    fun user(userID: String): DocumentReference

    fun feeds(): Listing<FeedItem>

    fun sellerItems(sellerName: String) : Listing<FeedItem>

    fun sellers(sellerName: String) : Listing<FeedItem>

    fun rigs(): Listing<Rig>

    fun followed(): Listing<Saved>

    suspend fun saveItem(feedItem: FeedItem)

    fun deleteRig(firebaseUser: FirebaseUser, rigId: String): Task<Void>

    fun deleteSaved(firebaseUser: FirebaseUser, savedId: String): Task<Void>

    fun rigParts(rigId: String) : Listing<RigPart>

    fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void>

    fun searchFeeds(searchVal: String): Listing<FeedItem>

    fun addRigPart(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: FeedItem
    ) : Task<Void>

    fun addSavedToRigPart(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: Saved
    ) : Task<Void>

    fun deleteRigPart(rigId: String, rigItemId: String): Task<Void>

    fun checkUserFollowedSeller(sellerName: String) : DocumentReference

    fun followSeller(sellerName: String) : Task<Void>

    fun unfollowSeller(sellerName: String) : Task<Void>

    fun nukeLoggedInUserData() : MutableLiveData<NetworkState>
}