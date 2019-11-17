package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.Config
import androidx.paging.toLiveData
import ariesvelasquez.com.republikapc.Const.RIGS_COLLECTION
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.db.TipidPCDatabase
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.Listing
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.rigs.RigDataSourceFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(reponse: FeedItemsResource) {
        reponse.items.let { posts ->
            db.runInTransaction {
                val itemsWithIndex = posts?.mapIndexed { index, child ->
                    child.indexInResponse = child.page
                    child
                }
                db.items().insert(itemsWithIndex!!).run {
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
    private fun refresh(): LiveData<NetworkState> {
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
                    ioExecutor.execute {
                        db.runInTransaction {
                            db.items().nukeFeedItems()
                            insertResultIntoDb(response.body()!!)
                        }
                        // since we are in bg thread now, post the result.
                        networkState.postValue(NetworkState.LOADED)
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
        val boundaryCallback = DashboardBoundaryCallback(
            webservice = tipidPCApi,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        // we are using a mutable live data to trigger refreshFeeds requests which eventually calls
        // refreshFeeds method and gets a new live data. Each refreshFeeds request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh()
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

    override fun createRig(firebaseUser: FirebaseUser, rigName: String): Task<Void> {
        // Write a new Rig with the users data.
        val createRigLiveData = MutableLiveData<NetworkState>()
        createRigLiveData.postValue(NetworkState.LOADING)

        val rigRef = firestore.collection(RIGS_COLLECTION).document()
        val newRigId = rigRef.id

        val newRigMap = hashMapOf<String, Any?>()
        newRigMap["name"] = rigName
        newRigMap["id"] = newRigId
        newRigMap["owner_id"] = firebaseUser.uid
        newRigMap["owner_name"] = firebaseUser.displayName
        newRigMap["owner_thumb"] = firebaseUser.photoUrl.toString()

        return rigRef.set(newRigMap)
    }
}





























