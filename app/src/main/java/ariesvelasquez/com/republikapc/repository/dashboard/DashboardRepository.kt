package ariesvelasquez.com.republikapc.repository.dashboard

import android.annotation.SuppressLint
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import ariesvelasquez.com.republikapc.Const.FIRST_LETTER
import ariesvelasquez.com.republikapc.Const.FOLLOWED_COLLECTION
import ariesvelasquez.com.republikapc.Const.NAME
import ariesvelasquez.com.republikapc.Const.OWNER_ID
import ariesvelasquez.com.republikapc.Const.RIGS_COLLECTION
import ariesvelasquez.com.republikapc.Const.RIGS_ITEM_COLLECTION
import ariesvelasquez.com.republikapc.Const.SAVED_COLLECTION
import ariesvelasquez.com.republikapc.Const.SELLER
import ariesvelasquez.com.republikapc.Const.USERS_COLLECTION
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.datasource.local.RepublikaPCDatabase
import ariesvelasquez.com.republikapc.datasource.firestore.saved.SavedFirestoreService
import ariesvelasquez.com.republikapc.model.error.Error
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.Listing
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.dashboard.feeds.FeedBoundaryCallback
import ariesvelasquez.com.republikapc.repository.dashboard.rigitems.RigItemsBoundaryCallback
import ariesvelasquez.com.republikapc.repository.followed.FollowedDataSourceFactory
import ariesvelasquez.com.republikapc.repository.rigs.RigDataSourceFactory
import ariesvelasquez.com.republikapc.repository.dashboard.selleritems.SellerItemsBoundaryCallback
import ariesvelasquez.com.republikapc.repository.search.SearchSellerSourceFactory
import ariesvelasquez.com.republikapc.repository.search.SearchSourceFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executor

class DashboardRepository(
    val db: RepublikaPCDatabase,
    private val savedFirestoreService: SavedFirestoreService,
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
                        child.lastRefresh = Date().toString()
                        child.indexInResponse = child.page
                        child
                    }!!
//                }

                db.items().insertFeeds(itemsWithIndex).run {
                    Timber.e("Inserted new item with page %s", itemsWithIndex.size)
                }
            }
        }
    }

    /**
     * When refreshFeeds is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insertFeeds all new items in a transaction.
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
        val boundaryCallback =
            FeedBoundaryCallback(
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
        val livePagedList = db.items().feedItems().toLiveData(
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
     * the database table and insertFeeds all new items in a transaction.
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

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    @SuppressLint("DefaultLocale")
    private fun insertSavedIntoDb(list: List<Saved>) {
//        db.runInTransaction {
//
//            val itemWithFirstLetterIndex = list.mapIndexed { index, savedItem ->
//                savedItem.firstLetterIndex = savedItem.name?.first()?.toUpperCase().toString()
//                savedItem
//            }

//            db.items().insertSaved(itemWithFirstLetterIndex).run {
//                Timber.e("Inserted new saved items %s", list.size)
//            }
//        }
    }

//    override fun saved(): Listing<FeedItem> {
//        // create a boundary callback which will observe when the user reaches to the edges of
//        // the list and update the database with extra data.
//        val boundaryCallback =
//            FeedBoundaryCallback(
//                savedReference = firestore.collection(SAVED_COLLECTION),
//                handleResponse = this::insertSavedIntoDb,
//                ioExecutor = ioExecutor
//            )
//
//        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
//        val livePagedList = db.items().feedItems().toLiveData(
//            pageSize = 10,
//            boundaryCallback = boundaryCallback
//        )
//
//        return Listing(
//            pagedList = livePagedList,
//            networkState = boundaryCallback.networkState,
//            retry = {
//                boundaryCallback.helper.retryAllFailed()
//            },
//            refresh = {},
//            refreshState = MutableLiveData()
//        )
//    }

    @SuppressLint("DefaultLocale")
    override suspend fun saveItem(feedItem: FeedItem) {
        return savedFirestoreService.saveFeedItem(feedItem)
    }

    override fun deleteSaved(firebaseUser: FirebaseUser, savedId: String): Task<Void> {
        val batchWrite: WriteBatch = firestore.batch()
        val savedRef = firestore.collection(SAVED_COLLECTION).document(savedId)
        batchWrite.delete(savedRef)

        // Remove From Room
        ioExecutor.execute {
            db.runInTransaction {
//                db.items().removeItem(savedId)
            }
        }

        return batchWrite.commit()
    }

    override fun followed(): Listing<Saved> {
        val sourceFactory = FollowedDataSourceFactory(firestore.collection(
            FOLLOWED_COLLECTION))

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

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertIntooRigPartsDb(list: List<RigPart>) {
        db.runInTransaction {
            db.items().insertRigParts(list).run {
                Timber.e("Inserted new rig parts %s", list.size)
            }
        }
    }

    override fun rigParts(rigId: String): Listing<RigPart> {

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback =
            RigItemsBoundaryCallback(
                rigItemDocId = rigId,
                rigItemsRef = firestore.collection(RIGS_ITEM_COLLECTION),
                handleResponse = this::insertIntooRigPartsDb,
                ioExecutor = ioExecutor
            )

        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.items().rigParts(rigId).toLiveData(
            pageSize = 10,
            boundaryCallback = boundaryCallback
        )

        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {},
            refreshState = MutableLiveData()
        )
    }

    override fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        val rigRef = firestore.collection(RIGS_COLLECTION).document()

        val newRigMap = hashMapOf<String, Any?>()
        newRigMap[NAME] = rigName
        newRigMap["id"] = rigRef.id
        newRigMap[OWNER_ID] = firebaseUser.uid
        newRigMap["ownerName"] = firebaseUser.displayName
        newRigMap["ownerThumb"] = firebaseUser.photoUrl.toString()
        newRigMap["date"] = FieldValue.serverTimestamp()
        newRigMap["itemCount"] = 0
        batchWrite.set(rigRef, newRigMap)

        val userRigCountRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
        batchWrite.update(userRigCountRef, "rigCount", FieldValue.increment(1))

        return batchWrite.commit()
    }

    override fun addRigPart(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: FeedItem
    ): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Counter Ref
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigItem.id)
        val rigItemRef = firestore.collection(RIGS_ITEM_COLLECTION).document(rigItem.id)
            .collection(RIGS_ITEM_COLLECTION).document()

        val rigPartMap = hashMapOf<String, Any?>()
        rigPartMap["name"] = feedItem.name
        rigPartMap[SELLER] = feedItem.seller
        rigPartMap["price"] = feedItem.price?.replace("PHP", "")?.replace("P", "")
        rigPartMap["docId"] = rigItemRef.id
        rigPartMap["postDate"] = feedItem.date
        rigPartMap["linkId"] = feedItem.linkId

        batchWrite.update(rigRef, "itemCount", FieldValue.increment(1))
        batchWrite.set(rigItemRef, rigPartMap)

        // ADd To Room
        ioExecutor.execute {
            db.runInTransaction {
                db.items().insertRigPart(RigPart().mapToObject(rigPartMap).apply {
                    rigParentId = rigItem.id
                    firstLetterIndex = this.name.first().toUpperCase().toString()
                })
            }
        }

        return batchWrite.commit()
    }

    override fun addSavedToRigPart(
        firebaseUser: FirebaseUser,
        rigItem: Rig,
        feedItem: Saved
    ): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Counter Ref
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigItem.id)
        val rigItemRef = firestore.collection(RIGS_ITEM_COLLECTION).document(rigItem.id)
            .collection(RIGS_ITEM_COLLECTION).document()

        val rigPartMap = hashMapOf<String, Any?>()
        rigPartMap["name"] = feedItem.name
        rigPartMap[SELLER] = feedItem.seller
        rigPartMap["price"] = feedItem.price?.replace("PHP", "")?.replace("P", "")
        rigPartMap["docId"] = rigItemRef.id
        rigPartMap["postDate"] = feedItem.date
        rigPartMap["linkId"] = feedItem.linkId

        batchWrite.update(rigRef, "itemCount", FieldValue.increment(1))
        batchWrite.set(rigItemRef, rigPartMap)

        // ADd To Room
        ioExecutor.execute {
            db.runInTransaction {
                db.items().insertRigPart(RigPart().mapToObject(rigPartMap).apply {
                    rigParentId = rigItem.id
                    firstLetterIndex = this.name.first().toUpperCase().toString()
                })
            }
        }

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

    override fun deleteRigPart(rigId: String, partId: String): Task<Void> {

        val batchWrite: WriteBatch = firestore.batch()

        // Delete Rig Item Task
        val rigRef = firestore.collection(RIGS_COLLECTION).document(rigId)
        val rigItemRef = firestore
            .collection(RIGS_ITEM_COLLECTION).document(rigId)
            .collection(RIGS_ITEM_COLLECTION).document(partId)
        batchWrite.delete(rigItemRef)

        // Decrement Rig Item Count Task
        batchWrite.update(rigRef, "itemCount", FieldValue.increment(-1))

        // Remove From Room
        ioExecutor.execute {
            db.runInTransaction {
                db.items().removeRigPart(rigId = rigId, partId = partId)
            }
        }

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

        return firestore.collection(FOLLOWED_COLLECTION).document(combinedUID)
    }

    override fun followSeller(sellerName: String): Task<Void> {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val combinedUID = firebaseUser!!.uid + "_" + sellerName

        val followedTpcSellerRef = firestore.collection(FOLLOWED_COLLECTION).document(combinedUID)

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

        val followedTpcSellerRef = firestore.collection(FOLLOWED_COLLECTION).document(combinedUID)

        return followedTpcSellerRef.delete()
    }

    override fun nukeLoggedInUserData() : MutableLiveData<NetworkState> {
        Timber.e("nukeLoggedInUserData")
        val networkState = MutableLiveData<NetworkState>()

        networkState.value = NetworkState.LOADING

        ioExecutor.execute {
            db.runInTransaction( Callable {
                db.items().nukeAllRigParts()
            })
            networkState.postValue(NetworkState.LOADED)
        }

        return networkState
    }
}



/**
 * When refreshRigParts is called, we simply run a fresh network request and when it arrives, clear
 * the database table and insertFeeds all new items in a transaction.
 * <p>
 * Since the PagedList already uses a database bound data source, it will automatically be
 * updated after the database transaction is finished.
 */
//@MainThread
//private fun refreshRigParts(rigItemDocId: String): LiveData<NetworkState> {
//    val networkState = MutableLiveData<NetworkState>()
//    networkState.value = NetworkState.LOADING
//
//    val rigItemsRef = firestore.collection(RIGS_ITEM_COLLECTION)
//
//    val initialQuery = rigItemsRef.document(rigItemDocId)
//        .collection(RIGS_ITEM_COLLECTION)
//        .limit(ITEM_PER_PAGE_20)
//
//    try {
//        initialQuery.get().addOnCompleteListener { task ->
//            val rigPartList = mutableListOf<RigPart>()
//            if (task.isSuccessful) {
//
//                val querySnapshot = task.result
//
//                for (document in querySnapshot!!) {
//                    val savedItem = document.toObject(RigPart::class.java)
//                    rigPartList.add(savedItem)
//                }
//
//                ioExecutor.execute {
//                    db.runInTransaction {
//                        db.items().nukeRigParts(rigItemDocId)
//                        insertIntooRigPartsDb(rigPartList)
//                    }
//                    // since we are in bg thread now, post the result.
//                    networkState.postValue(NetworkState.LOADED)
//                }
//            } else {
//                networkState.value = NetworkState.error(task.exception?.message)
//            }
//        }
//    } catch (ioException: IOException) {
//        networkState.value = NetworkState.error(ioException.message)
//    }
//
//    return networkState
//}


























