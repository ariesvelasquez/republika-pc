package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import ariesvelasquez.com.republikapc.Const.RIGS_COLLECTION
import ariesvelasquez.com.republikapc.model.rigs.RigItem
import ariesvelasquez.com.republikapc.repository.dashboard.IDashboardRepository
import ariesvelasquez.com.republikapc.repository.rigs.RigDataSourceFactory
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

class DashboardViewModel(private val repository: IDashboardRepository) : ViewModel() {


    private val isFeedsInitialized = MutableLiveData<Boolean>()
    private val feedsRepoResult = Transformations.map(isFeedsInitialized) { repository.feeds() }
    val feedItems = Transformations.switchMap(feedsRepoResult) { it.pagedList }!!
    val feedsNetworkState = Transformations.switchMap(feedsRepoResult) { it.networkState }!!
    val feedsRefreshState = Transformations.switchMap(feedsRepoResult) { it.refreshState }!!

    private val isRigsInitialized = MutableLiveData<Boolean>()
    private val rigRepoResult = Transformations.map(isRigsInitialized) { repository.rigs() }
    val rigItems = Transformations.switchMap(rigRepoResult) { it.pagedList }!!
    val rigNetworkState = Transformations.switchMap(rigRepoResult) { it.networkState }!!
    val rigRefreshState = Transformations.switchMap(rigRepoResult) { it.refreshState }!!

    val userModel = MutableLiveData<FirebaseUser>()
    val isUserSignedIn = MutableLiveData<Boolean>()

    // FEEDS
    fun refreshFeeds() {
        feedsRepoResult.value?.refresh?.invoke()
    }

    fun showFeedItems(): Boolean {
        this.isFeedsInitialized.value = true
        return true
    }

    fun retryFeeds() {
        val listing = feedsRepoResult?.value
        listing?.retry?.invoke()
    }

    // RIGS_COLLECTION
    fun refreshRigs() {
        rigRepoResult.value?.refresh?.invoke()
    }

    fun showRigItems(): Boolean {
        this.isRigsInitialized.value = true
        return true
    }

    fun cancelRigs() {
        this.isRigsInitialized.value = false
    }

    // USER
    fun setUser(firebaseUser: FirebaseUser) {
        this.userModel.value = firebaseUser
    }

    fun setIsUserSignedIn(isUserSignedIn: Boolean) {
        this.isUserSignedIn.value = isUserSignedIn
    }
}