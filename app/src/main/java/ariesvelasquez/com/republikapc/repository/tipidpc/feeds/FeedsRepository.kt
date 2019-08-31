package ariesvelasquez.com.republikapc.repository.tipidpc.feeds

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.db.TipidPCDatabase
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.repository.Listing
import ariesvelasquez.com.republikapc.repository.NetworkState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Executor

class FeedsRepository(
    val db: TipidPCDatabase,
    private val tipidPCApi: TipidPCApi,
    private val ioExecutor: Executor
) : IFeedsRepository {

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
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
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

                override fun onResponse(call: Call<FeedItemsResource>, response: Response<FeedItemsResource>) {
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
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    override fun feeds(): Listing<FeedItem> {

        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = FeedsBoundaryCallback(
            webservice = tipidPCApi,
            handleResponse = this::insertResultIntoDb,
            ioExecutor = ioExecutor
        )
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
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

}