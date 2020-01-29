package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import ariesvelasquez.com.republikapc.Const.FIRST_LETTER
import ariesvelasquez.com.republikapc.Const.FOLLOWED_TPC_SELLER_COLLECTION
import ariesvelasquez.com.republikapc.Const.OWNER_ID
import ariesvelasquez.com.republikapc.Const.RIGS_COLLECTION
import ariesvelasquez.com.republikapc.Const.RIGS_ITEM_COLLECTION
import ariesvelasquez.com.republikapc.Const.SAVED_COLLECTION
import ariesvelasquez.com.republikapc.Const.SELLER
import ariesvelasquez.com.republikapc.Const.USERS_COLLECTION
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.db.TipidPCDatabase
import ariesvelasquez.com.republikapc.model.error.Error
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.Listing
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.followed.FollowedDataSourceFactory
import ariesvelasquez.com.republikapc.repository.rigs.RigDataSourceFactory
import ariesvelasquez.com.republikapc.repository.rigs.RigItemsDataSourceFactory
import ariesvelasquez.com.republikapc.repository.saved.SavedDataSourceFactory
import ariesvelasquez.com.republikapc.repository.search.SearchSellerSourceFactory
import ariesvelasquez.com.republikapc.repository.search.SearchSourceFactory
import ariesvelasquez.com.republikapc.utils.translateBack
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Executor

class DashboardRepository(
    val db: TipidPCDatabase,
    private val firestore: FirebaseFirestore,
    private val tipidPCApi: TipidPCApi,
    private val ioExecutor: Executor
) : IDashboardRepository {

    override fun user(userID: String): DocumentReference {
        return firestore.collection(USERS_COLLECTION).document(userID)
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(response: FeedItemsResource) {
        Timber.e("insertResultIntoDb")
        response.items.let { posts ->
            db.runInTransaction {
//                val itemsWithIndex: List<FeedItem>

                var itemsWithIndex = response.items ?: emptyList()

                // Lsst Item View Type
//                if (response.isListEmpty && response.page.toInt() > 1) {
//                    // Add a last item model Type
//                    (itemsWithIndex as ArrayList).add(FeedItem(isLastItem = true))
//                } else if (response.isListEmpty && response.page.toInt() == 1) {
//                    Timber.e("no item found else")
//                    // Add a No Item Found Model Type
//                    (itemsWithIndex as ArrayList).add(FeedItem(isEmptyItem = true))
//                } else {
                    itemsWithIndex = posts?.mapIndexed { index, child ->
                        child.indexInResponse = child.page
                        child
                    }!!
//                }

                db.items().insert(itemsWithIndex).run {
                    Timber.e("Inserted new item with page %s", itemsWithIndex.size)
                }
            }
        }
    }

    /**
     * When refreshFeeds is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refreshFeeds(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        tipidPCApi.run {
            networkState.value = NetworkState.LOADING
            getSellingItems(1).enqueue(object : Callback<FeedItemsResource> {
                override fun onFailure(call: Call<FeedItemsResource>, t: Throwable) {
                    // retrofit calls this on main thread so safe to call set value
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(
                    call: Call<FeedItemsResource>,
                    response: Response<FeedItemsResource>
                ) {
                    if (response.isSuccessful) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                db.items().nukeFeedItems()
                                insertResultIntoDb(response.body()!!)
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } else {
                        val errorResponse = Gson().fromJson(response.errorBody()!!.string(), Error::class.java)
                        networkState.value = NetworkState.error(errorResponse.error)
                    }
                }
            })
        }
        return networkState
    }

    /**
     * Returns a Listing of tipid pc items.
     */
    @MainThread
    override fun feeds(): Listing<FeedItem> {

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = FeedBoundaryCallback(
            webservice = tipidPCApi,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        // we are using a mutable live data to trigger refreshFeeds requests which eventually calls
        // refreshFeeds method and gets a new live data. Each refreshFeeds request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshFeeds()
        }

        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.items().feedItems().toLiveData(
            pageSize = 40,
            boundaryCallback = boundaryCallback
        )

        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    /**
     * Returns a Listing of tipid pc items.
     */
    @MainThread
    override fun sellerItems(sellerName: String): Listing<FeedItem> {

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = SellerItemsBoundaryCallback(
            sellerName,
            webservice = tipidPCApi,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        // we are using a mutable live data to trigger refreshSellerItems requests which eventually calls
        // refreshSellerItems method and gets a new live data. Each refreshSellerItems request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refreshSellerItems(sellerName)
        }

        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.items().sellerItems(sellerName).toLiveData(
            pageSize = 7000,
            boundaryCallback = boundaryCallback
        )

        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    /**
     * When refreshFeeds is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refreshSellerItems(sellerName: String): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        tipidPCApi.run {
            networkState.value = NetworkState.LOADING
            Timber.e("refreshSellerItems repo")
            getSellerItems(sellerName).enqueue(object : Callback<FeedItemsResource> {
                override fun onFailure(call: Call<FeedItemsResource>, t: Throwable) {
                    // retrofit calls this on main thread so safe to call set value
                    networkState.value = NetworkState.error(t.message)
                }

                override fun onResponse(
                    call: Call<FeedItemsResource>,
                    response: Response<FeedItemsResource>
                ) {
                    if (response.isSuccessful) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                db.items().deleteOldSellerItems(sellerName)
                                insertResultIntoDb(response.body()!!)
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    } else {
                        val errorResponse = Gson().fromJson(response.errorBody()!!.string(), Error::class.java)
                        networkState.value = NetworkState.error(errorResponse.error)
                    }
                }
            })
        }
        return networkState
    }

    override fun rigs(): Listing<Rig> {
        val sourceFactory = RigDataSourceFactory(firestore.collection(RIGS_COLLECTION))

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            )
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                //                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    override fun saved(): Listing<Saved> {
        val sourceFactory = SavedDataSourceFactory(firestore.collection(SAVED_COLLECTION))

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            )
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                //                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    override fun saveItem(firebaseUser: FirebaseUser, feedItem: FeedItem): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Counter Ref
        val savedRef = firestore.collection(SAVED_COLLECTION).document()

        val saveItemMap = hashMapOf<String, Any?>()
        saveItemMap["docId"] = savedRef.id
        saveItemMap[OWNER_ID] = firebaseUser.uid
        saveItemMap["name"] = feedItem.name
        saveItemMap[SELLER] = feedItem.seller
        saveItemMap["price"] = feedItem.price.replace("PHP", "").replace("P", "")
        saveItemMap["postDate"] = feedItem.date
        saveItemMap["linkId"] = feedItem.linkId

        batchWrite.set(savedRef, saveItemMap)

        return batchWrite.commit()
    }

    override fun followed(): Listing<Saved> {
        val sourceFactory = FollowedDataSourceFactory(firestore.collection(
            FOLLOWED_TPC_SELLER_COLLECTION))

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            )
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                //                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    override fun rigItems(rigId: String): Listing<FeedItem> {
        val sourceFactory =
            RigItemsDataSourceFactory(firestore.collection(RIGS_ITEM_COLLECTION), rigId)

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            )
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                //                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    override fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        val rigRef = firestore.collection(RIGS_COLLECTION).document()

        val newRigMap = hashMapOf<String, Any?>()
        newRigMap["name"] = rigName
        newRigMap["id"] = rigRef.id
        newRigMap["ownerId"] = firebaseUser.uid
        newRigMap["ownerName"] = firebaseUser.displayName
        newRigMap["ownerThumb"] = firebaseUser.photoUrl.toString()
        newRigMap["date"] = FieldValue.serverTimestamp()
        newRigMap["itemCount"] = 0
        batchWrite.set(rigRef, newRigMap)

        val userRigCountRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        batchWrite.update(userRigCountRef, "rigCount", FieldValue.increment(1))

        return batchWrite.commit()
    }

    override fun addItemToRig(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: FeedItem
    ): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Counter Ref
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigItem.id)
        val rigItemRef = firestore.collection(RIGS_ITEM_COLLECTION).document(rigItem.id)
            .collection(RIGS_ITEM_COLLECTION).document()

        val feedItemMap = hashMapOf<String, Any?>()
        feedItemMap["id"] = feedItem.id
        feedItemMap["name"] = feedItem.name
        feedItemMap[SELLER] = feedItem.seller
        feedItemMap["price"] = feedItem.price.replace("PHP", "").replace("P", "")
        feedItemMap["docId"] = rigItemRef.id
        feedItemMap["postDate"] = feedItem.date
        feedItemMap["linkId"] = feedItem.linkId

        batchWrite.update(rigRef, "itemCount", FieldValue.increment(1))
        batchWrite.set(rigItemRef, feedItemMap)

        return batchWrite.commit()
    }

    override fun addSavedItemToRig(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: Saved
    ): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Counter Ref
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigItem.id)
        val rigItemRef = firestore.collection(RIGS_ITEM_COLLECTION).document(rigItem.id)
            .collection(RIGS_ITEM_COLLECTION).document()

        val feedItemMap = hashMapOf<String, Any?>()
        feedItemMap["name"] = feedItem.name
        feedItemMap[SELLER] = feedItem.seller
        feedItemMap["price"] = feedItem.price.replace("PHP", "").replace("P", "")
        feedItemMap["docId"] = rigItemRef.id
        feedItemMap["postDate"] = feedItem.date
        feedItemMap["linkId"] = feedItem.linkId

        batchWrite.update(rigRef, "itemCount", FieldValue.increment(1))
        batchWrite.set(rigItemRef, feedItemMap)

        return batchWrite.commit()
    }

    override fun deleteRig(firebaseUser: FirebaseUser, rigId: String): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigId)
        val rigItemsRef = firestore.collection(RIGS_ITEM_COLLECTION).document(rigId)
        val userRigCountRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)

        batchWrite.delete(rigRef)
        batchWrite.delete(rigItemsRef)
        batchWrite.update(userRigCountRef, "rigCount", FieldValue.increment(-1))

        return batchWrite.commit()
    }

    override fun deleteSaved(firebaseUser: FirebaseUser, savedId: String): Task<Void> {
        val batchWrite: WriteBatch = firestore.batch()
        val savedRef = firestore.collection(SAVED_COLLECTION).document(savedId)
        batchWrite.delete(savedRef)

        return batchWrite.commit()
    }

    override fun deleteRigItem(rigId: String, rigItemId: String): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Delete Rig Item Task
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigId)
        val rigItemRef = firestore
            .collection(RIGS_ITEM_COLLECTION).document(rigId)
            .collection(RIGS_ITEM_COLLECTION).document(rigItemId)
        batchWrite.delete(rigItemRef)

        // Decrement Rig Item Count Task
        batchWrite.update(rigRef, "itemCount", FieldValue.increment(-1))

        return batchWrite.commit()
    }

    @MainThread
    override fun searchFeeds(searchVal: String): Listing<FeedItem> {
        val sourceFactory = SearchSourceFactory(tipidPCApi, searchVal, ioExecutor)

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            ),
            // provide custom executor for network requests, otherwise it will default to
            // Arch Components' IO pool which is also used for disk access
            fetchExecutor = ioExecutor
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    @MainThread
    override fun sellers(sellerName: String): Listing<FeedItem> {
        val sourceFactory = SearchSellerSourceFactory(tipidPCApi, sellerName, ioExecutor)

        // We use toLiveData Kotlin ext. function here, you could also use LivePagedListBuilder
        val livePagedList = sourceFactory.toLiveData(
            // we use Config Kotlin ext. function here, could also use PagedList.Config.Builder
            config = Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10 * 2
            ),
            // provide custom executor for network requests, otherwise it will default to
            // Arch Components' IO pool which is also used for disk access
            fetchExecutor = ioExecutor
        )

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
            pagedList = livePagedList,
            networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                it.networkState
            },
            retry = {
                sourceFactory.sourceLiveData.value?.retryAllFailed()
            },
            refresh = {
                sourceFactory.sourceLiveData.value?.invalidate()
            },
            refreshState = refreshState
        )
    }

    override fun checkUserFollowedSeller(sellerName: String): DocumentReference {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val combinedUID = firebaseUser!!.uid + "_" + sellerName

        return firestore.collection(FOLLOWED_TPC_SELLER_COLLECTION).document(combinedUID)
    }

    override fun followSeller(sellerName: String): Task<Void> {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val combinedUID = firebaseUser!!.uid + "_" + sellerName

        val followedTpcSellerRef = firestore.collection(FOLLOWED_TPC_SELLER_COLLECTION).document(combinedUID)

        val followItemMap = hashMapOf<String, Any?>()
        followItemMap[OWNER_ID] = firebaseUser.uid
        followItemMap[SELLER] = sellerName
        followItemMap["docId"] = combinedUID
        followItemMap[FIRST_LETTER] = sellerName.first().toUpperCase().toString()

        return followedTpcSellerRef.set(followItemMap)
    }

    override fun unfollowSeller(sellerName: String): Task<Void> {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val combinedUID = firebaseUser!!.uid + "_" + sellerName

        val followedTpcSellerRef = firestore.collection(FOLLOWED_TPC_SELLER_COLLECTION).document(combinedUID)

        return followedTpcSellerRef.delete()
    }
}





























