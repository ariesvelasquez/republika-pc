package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.model.user.User
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.dashboard.IDashboardRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.MetadataChanges

class DashboardViewModel(private val repository: IDashboardRepository) : ViewModel() {

    /**
     * Feeds Vars
     */
    private val isFeedsInitialized = MutableLiveData<Boolean>()
    private val feedsRepoResult = map(isFeedsInitialized) { repository.feeds() }
    val feedItems = Transformations.switchMap(feedsRepoResult) { it.pagedList }
    val feedsNetworkState = Transformations.switchMap(feedsRepoResult) { it.networkState }
    val feedsRefreshState = Transformations.switchMap(feedsRepoResult) { it.refreshState }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Seller Items Vars
     */
    private var sellerName: String = ""
    private val isSellerItemsInitialized = MutableLiveData<Boolean>()
    private val sellerItemsRepoResult = map(isSellerItemsInitialized) { repository.sellerItems(sellerName) }
    val sellerItems = Transformations.switchMap(sellerItemsRepoResult) { it.pagedList }
    val sellerItemsNetworkState = Transformations.switchMap(sellerItemsRepoResult) { it.networkState }
    val sellerItemsRefreshState = Transformations.switchMap(sellerItemsRepoResult) { it.refreshState }

    // Seller Items
    fun refreshSellerItems() {
        sellerItemsRepoResult.value?.refresh?.invoke()
    }

    fun showSellerItems(sellerName: String): Boolean {
        this.sellerName = sellerName
        this.isSellerItemsInitialized.value = true
        return true
    }

    fun retrySellerItems() {
        val listing = sellerItemsRepoResult.value
        listing?.retry?.invoke()
    }

    /**
     * Rigs Vars
     */
    val isRigsInitialized = MutableLiveData<Boolean>()
    private val rigRepoResult = map(isRigsInitialized) { repository.rigs() }
    val rigs = Transformations.switchMap(rigRepoResult) { it.pagedList }
    val rigNetworkState = Transformations.switchMap(rigRepoResult) { it.networkState }
    val rigRefreshState = Transformations.switchMap(rigRepoResult) { it.refreshState }
    val createRigNetworkState = MutableLiveData<NetworkState>()
    val deleteRigNetworkState = MutableLiveData<NetworkState>()

    // RIGS_COLLECTION
    // Create Rig
    fun createRig(rigName: String) {
        createRigNetworkState.postValue(NetworkState.LOADING)
        repository.createRig(this.firebaseUserModel.value!!, rigName).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
            refreshRigs()
            createRigNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            createRigNetworkState.postValue(error)
        }
    }

    fun refreshRigs() {
        rigRepoResult.value?.refresh?.invoke()
    }

    fun showRigs(): Boolean {
        this.isRigsInitialized.value = true
        return true
    }

    fun cancelRigs() {
        this.isRigsInitialized.value = false
    }

    fun deleteRig(rigId: String) {
        deleteRigNetworkState.postValue(NetworkState.LOADING)

        repository.deleteRig(this.firebaseUserModel.value!!, rigId).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
            refreshRigs()
            deleteRigNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            deleteRigNetworkState.postValue(error)
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Rig Item Vars
     */
    private var rigId: String = ""
    private val isRigItemsInitialized = MutableLiveData<Boolean>()
    private val rigItemsRepoResult = map(isRigItemsInitialized) { repository.rigItems(rigId) }
    val rigItems = Transformations.switchMap(rigItemsRepoResult) { it.pagedList }
    val rigItemsNetworkState = Transformations.switchMap(rigItemsRepoResult) { it.networkState }
    val rigItemsRefreshState = Transformations.switchMap(rigItemsRepoResult) { it.refreshState }
    val addItemToRigNetworkState = MutableLiveData<NetworkState>()
    val deleteRigItemNetworkState = MutableLiveData<NetworkState>()

    // RIG_ITEMS_COLLECTION
    fun getRigItems(rigId: String): Boolean {
        this.rigId = rigId
        this.isRigItemsInitialized.value = true
//        this.rigItemsRepoResult.value?.refresh?.invoke()
        return true
    }

    // Add Item To Rig
    fun addItemToRig(rigItem: Rig, feedItem: FeedItem) {
        addItemToRigNetworkState.postValue(NetworkState.LOADING)
        repository.addItemToRig(this.firebaseUserModel.value!!, rigItem, feedItem)
            .addOnSuccessListener {
                RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
                refreshRigItems()
                addItemToRigNetworkState.postValue(NetworkState.LOADED)
            }.addOnFailureListener {
                val error = NetworkState.error(it.message)
                addItemToRigNetworkState.postValue(error)
            }
    }

    // Add Item To Rig
    fun addSavedItemToRig(rigItem: Rig, savedItem: Saved) {
        addItemToRigNetworkState.postValue(NetworkState.LOADING)
        repository.addSavedItemToRig(this.firebaseUserModel.value!!, rigItem, savedItem)
            .addOnSuccessListener {
                RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
                refreshRigItems()
                addItemToRigNetworkState.postValue(NetworkState.LOADED)
            }.addOnFailureListener {
                val error = NetworkState.error(it.message)
                addItemToRigNetworkState.postValue(error)
            }
    }

    fun deleteRigItem(rigId: String, rigItemId: String) {
        deleteRigItemNetworkState.postValue(NetworkState.LOADING)
        repository.deleteRigItem(rigId, rigItemId).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Saved Items Vars
     */
    val isSavedInitialized = MutableLiveData<Boolean>()
    private val savedRepoResult = map(isSavedInitialized) { repository.saved() }
    val saved = Transformations.switchMap(savedRepoResult) { it.pagedList }
    val savedNetworkState = Transformations.switchMap(savedRepoResult) { it.networkState }
    val savedRefreshState = Transformations.switchMap(savedRepoResult) { it.refreshState }
    val saveItemNetworkState = MutableLiveData<NetworkState>()
    val deleteSavedItemNetworkState = MutableLiveData<NetworkState>()

    fun save(feedItem: FeedItem) {
        saveItemNetworkState.postValue(NetworkState.LOADING)

        repository.saveItem(this.firebaseUserModel.value!!, feedItem).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshSaved = true
            refreshSaved()
            saveItemNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            saveItemNetworkState.postValue(error)
        }
    }

    fun refreshSaved() {
        savedRepoResult.value?.refresh?.invoke()
    }

    fun showSaved(): Boolean {
        this.isSavedInitialized.value = true
        return true
    }

    fun cancelSaved() {
        this.isSavedInitialized.value = false
    }

    fun deleteSaved(savedId: String) {
        deleteSavedItemNetworkState.postValue(NetworkState.LOADING)

        repository.deleteSaved(this.firebaseUserModel.value!!, savedId).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshSaved = true
            refreshSaved()
            deleteSavedItemNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            deleteSavedItemNetworkState.postValue(error)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Followed Items Vars
     */
    val isFollowedInitialized = MutableLiveData<Boolean>()
    private val followedRepoResult = map(isFollowedInitialized) { repository.followed() }
    val followed = Transformations.switchMap(followedRepoResult) { it.pagedList }
    val followedNetworkState = Transformations.switchMap(followedRepoResult) { it.networkState }
    val followedRefreshState = Transformations.switchMap(followedRepoResult) { it.refreshState }
    val followUnfollowSellerNetworkState = MutableLiveData<NetworkState>()

    fun refreshFollowed() {
        followedRepoResult.value?.refresh?.invoke()
    }

    fun showFollowed(): Boolean {
        this.isFollowedInitialized.value = true
        return true
    }

    fun cancelFollowed() {
        this.isFollowedInitialized.value = false
    }

    val isUserFollowedSeller = MutableLiveData<Boolean>()
    val checkUserFollowedState = MutableLiveData<NetworkState>()

    fun checkIfUserFollowedSeller(sellerName: String) {
        isUserFollowedSeller.value = false
        checkUserFollowedState.postValue(NetworkState.LOADING)

        repository.checkUserFollowedSeller(sellerName).get().addOnSuccessListener {snapshot ->
            isUserFollowedSeller.value = snapshot.exists()
            checkUserFollowedState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            checkUserFollowedState.postValue(error)
        }
    }

    fun followSeller(sellerName: String) {
        followUnfollowSellerNetworkState.postValue(NetworkState.LOADING)
        repository.followSeller(sellerName).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshFollowed = true
            refreshFollowed()
            followUnfollowSellerNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            followUnfollowSellerNetworkState.postValue(error)
        }
    }

    fun unfollowSeller(sellerName: String) {
        followUnfollowSellerNetworkState.postValue(NetworkState.LOADING)
        repository.unfollowSeller(sellerName).addOnSuccessListener {
            RepublikaPC.getGlobalFlags().shouldRefreshFollowed = true
            refreshFollowed()
            followUnfollowSellerNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            followUnfollowSellerNetworkState.postValue(error)
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Search Items Vars
     */

    private val searchVal = MutableLiveData<String>()
    private val searchRepoResult = map(searchVal) { repository.searchFeeds(it) }
    val searchItems = Transformations.switchMap(searchRepoResult) { it.pagedList }
    val searchNetworkState = Transformations.switchMap(searchRepoResult) { it.networkState }
    val searchRefreshNetworkState = Transformations.switchMap(searchRepoResult) { it.refreshState }

    fun refreshSearch() {
        searchRepoResult.value?.refresh?.invoke()
    }

    fun searchItems(searchVal: String) : Boolean {
        this.searchVal.value = searchVal
        return true
    }

    fun retrySearch() {
        val listing = searchRepoResult.value
        listing?.retry?.invoke()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Search Seller Vars
     */

    private val searchSellerVal = MutableLiveData<String>()
    private val searchSellerRepoResult = map(searchSellerVal) { repository.sellers(it) }
    val searchedSellers = Transformations.switchMap(searchSellerRepoResult) { it.pagedList }
    val searchedSellersNetworkState = Transformations.switchMap(searchSellerRepoResult) { it.networkState }
    val searchedSellersRefreshNetworkState = Transformations.switchMap(searchSellerRepoResult) { it.refreshState }



    fun refreshSearchedSellers() {
        searchSellerRepoResult.value?.refresh?.invoke()
    }

    fun searchSellers(sellerName: String) : Boolean {
        this.searchSellerVal.value = sellerName
        return true
    }

    fun retrySearchedSellers() {
        val listing = searchSellerRepoResult.value
        listing?.retry?.invoke()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * User Vars
     */
    val firebaseUserModel = MutableLiveData<FirebaseUser>()
    val isUserSignedIn = MutableLiveData<Boolean>()

    // USER
    fun setUser(firebaseUser: FirebaseUser) {
        this.firebaseUserModel.value = firebaseUser
    }

    fun setIsUserSignedIn(isUserSignedIn: Boolean) {
        this.isUserSignedIn.value = isUserSignedIn
    }

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
//                Timber.e("User Update Received userData rigCount " + userData!!.rigCount)
//                Timber.e("User Update Received user rigCount " + user.value?.rigCount)
            } else {
                userNetworkState.postValue(NetworkState.LOADED)
//                Timber.e("User not found in firestore db")
            }
        }
    }
}