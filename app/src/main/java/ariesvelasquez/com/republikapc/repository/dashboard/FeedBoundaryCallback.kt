package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.annotation.MainThread
import androidx.paging.PagedList
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.androidx.PagingRequestHelper
import ariesvelasquez.com.republikapc.model.error.Error
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.utils.createStatusLiveData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class FeedBoundaryCallback (
    private val webservice: TipidPCApi,
    private val handleResponse: (FeedItemsResource) -> Unit,
    private val ioExecutor: Executor
) : PagedList.BoundaryCallback<FeedItem>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    private var currentPage = 1

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.getSellingItems(1)
                .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: FeedItem) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            // Or notify user that all the items is loaded
            webservice.getSellingItems(itemAtEnd.page + 1)
                .enqueue(createWebserviceCallback(it))
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: FeedItem) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<FeedItemsResource> {
        return object : Callback<FeedItemsResource> {
            override fun onFailure(call: Call<FeedItemsResource>, t: Throwable) {
                it.recordFailure(t)
                Timber.e("FeedBoundaryCallback > createWebserviceCallback > ${t.message}")
            }

            override fun onResponse(
                call: Call<FeedItemsResource>, response: Response<FeedItemsResource>
            ) {
                if (response.isSuccessful) {
                    insertItemsIntoDb(response, it)
                } else {
                    val errorResponse = Gson().fromJson(response.errorBody()!!.string(), Error::class.java)
                    it.recordFailure(Throwable(errorResponse.error))
                }
            }
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        response: Response<FeedItemsResource>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(response.body()!!)
            it.recordSuccess()
        }
    }
}