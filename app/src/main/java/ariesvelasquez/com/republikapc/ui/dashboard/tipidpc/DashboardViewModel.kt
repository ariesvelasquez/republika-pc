package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import androidx.lifecycle.*
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.user.User
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.dashboard.IDashboardRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.MetadataChanges
import timber.log.Timber

class DashboardViewModel(private val repository: IDashboardRepository) : ViewModel() {

    /**
     * Feeds Vars
     */
    private val isFeedsInitialized = MutableLiveData<Boolean>()
    private val feedsRepoResult = Transformations.map(isFeedsInitialized) { repository.feeds() }
    val feedItems = Transformations.switchMap(feedsRepoResult) { it.pagedList }!!
    val feedsNetworkState = Transformations.switchMap(feedsRepoResult) { it.networkState }!!
    val feedsRefreshState = Transformations.switchMap(feedsRepoResult) { it.refreshState }!!

    /**
     * Rigs Vars
     */
    val isRigsInitialized = MutableLiveData<Boolean>()
    private val rigRepoResult = Transformations.map(isRigsInitialized) { repository.rigs() }
    val rigs = Transformations.switchMap(rigRepoResult) { it.pagedList }!!
    val rigNetworkState = Transformations.switchMap(rigRepoResult) { it.networkState }!!
    val rigRefreshState = Transformations.switchMap(rigRepoResult) { it.refreshState }!!
    val createRigNetworkState = MutableLiveData<NetworkState>()
    val addItemToRigNetworkState = MutableLiveData<NetworkState>()
    // After adding an item to a rig, update the flag that will check if rigs need to be refreshed
    var shouldRefreshRigs = false

    /**
     * Rig Item Vars
     */
    private var rigId: String = ""
    private val isRigItemsInitialized = MutableLiveData<Boolean>()
    private val rigItemsRepoResult = Transformations.map(isRigItemsInitialized) { repository.rigItems(rigId) }
    val rigItems = Transformations.switchMap(rigItemsRepoResult) { it.pagedList }!!
    val rigItemsNetworkState = Transformations.switchMap(rigItemsRepoResult) { it.networkState }!!
    val rigItemsRefreshState = Transformations.switchMap(rigItemsRepoResult) { it.refreshState }!!
    val deleteRigItemNetworkState = MutableLiveData<NetworkState>()
    
    /**
     * User Vars
     */
    val firebaseUserModel = MutableLiveData<FirebaseUser>()
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
        Timber.e("RefreshRigs()")
        rigRepoResult.value?.refresh?.invoke()
    }

    fun showRigs(): Boolean {
        this.isRigsInitialized.value = true
        return true
    }

    fun cancelRigs() {
        this.isRigsInitialized.value = false
    }

    // RIG_ITEMS_COLLECTION
    fun getRigItems(rigId: String): Boolean {
        Timber.e("view model getRigItems " + rigId)
        this.rigId = rigId
        this.isRigItemsInitialized.value = true
//        this.rigItemsRepoResult.value?.refresh?.invoke()
        return true
    }

    fun deleteRigItem(rigId: String, rigItemId: String) {
        Timber.e("deleting, rigId: $rigId | rigItemId: $rigItemId")
        deleteRigItemNetworkState.postValue(NetworkState.LOADING)

        repository.deleteRigItem(rigId, rigItemId).addOnSuccessListener {
            refreshRigItems()
            deleteRigItemNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            deleteRigItemNetworkState.postValue(error)
        }
    }

    fun refreshRigItems() {
        rigItemsRepoResult.value?.refresh?.invoke()
    }

    // USER
    fun setUser(firebaseUser: FirebaseUser) {
        this.firebaseUserModel.value = firebaseUser
    }

    fun setIsUserSignedIn(isUserSignedIn: Boolean) {
        this.isUserSignedIn.value = isUserSignedIn
    }

    // Create Rig
    fun createRig(rigName: String) {
        createRigNetworkState.postValue(NetworkState.LOADING)
        repository.createRig(this.firebaseUserModel.value!!, rigName).addOnSuccessListener {
            refreshRigs()
            createRigNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            createRigNetworkState.postValue(error)
        }
    }

    // Add Item To Rig
    fun addItemToRig(rigItem: Rig, feedItem: FeedItem) {
        addItemToRigNetworkState.postValue(NetworkState.LOADING)
        repository.addItemToRig(this.firebaseUserModel.value!!, rigItem, feedItem)
            .addOnSuccessListener {
                shouldRefreshRigs = true
                addItemToRigNetworkState.postValue(NetworkState.LOADED)
            }.addOnFailureListener {
                val error = NetworkState.error(it.message)
                addItemToRigNetworkState.postValue(error)
            }
    }

    /**
     * User
     */

    // Listener for User Updates
    val user = MutableLiveData<User>()
    val userNetworkState = MutableLiveData<NetworkState>()
    fun observeUser(userId: String) {
        userNetworkState.postValue(NetworkState.LOADING)

        repository.user(userId).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                val error = NetworkState.error(e.message)
                userNetworkState.postValue(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val userData = snapshot.toObject(User::class.java)
                user.value = userData
                userNetworkState.postValue(NetworkState.LOADED)
                Timber.e("User Update Received userData rigCount " + userData!!.rigCount)
                Timber.e("User Update Received user rigCount " + user.value?.rigCount)
            } else {
                userNetworkState.postValue(NetworkState.LOADED)
                Timber.e("User not found in firestore db")
            }
        }
    }
}