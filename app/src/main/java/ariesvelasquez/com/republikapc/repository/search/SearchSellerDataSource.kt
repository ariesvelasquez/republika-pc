package ariesvelasquez.com.republikapc.repository.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.model.error.Error
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.repository.NetworkState
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.Executor

class SearchSellerDataSource(
    val tipidPCApi: TipidPCApi,
    val sellerName: String,
    val retryExecutor: Executor
) : ItemKeyedDataSource<String, FeedItem>() {

    // keep a function reference for the retryFeeds event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     * <p>
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    private var currentPage = 1

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<String>, callback: LoadInitialCallback<FeedItem>) {
        // set network value to loading.
//        networkState.postValue(NetworkState.LOADING)

        val request = tipidPCApi.getSellers(sellerName)

        // update network states.
        // we also provide an initial load state to the listeners so that the UI can know when the
        // very first list is loaded.
//        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        // triggered by a refreshFeeds, we better execute sync
        try {
            val response = request.execute()

            if (response.isSuccessful) {
                val items = response.body()?.items ?: emptyList()
                retry = null
                initialLoad.postValue(NetworkState.LOADED)
                networkState.postValue(NetworkState.LOADED)

                if (items.isEmpty()) {
                    // Add An Empty Type Array List
                    (items as ArrayList).add(FeedItem(isEmptyItem = true))
                }

                callback.onResult(items)
            } else {
                retry = {
                    loadInitial(params, callback)
                }
                val errorResponse = Gson().fromJson(response.errorBody()!!.string(), Error::class.java)
                val error = NetworkState.error(errorResponse.error)
                networkState.postValue(error)
                initialLoad.postValue(error)
            }

        } catch (ioException: IOException) {
            retry = {
                loadInitial(params, callback)
            }
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<FeedItem>) {
        networkState.postValue(NetworkState.LOADED)
        initialLoad.postValue(NetworkState.LOADED)
//        // Increment the page
//        currentPage++
//        // set network value to loading.
//        networkState.postValue(NetworkState.LOADING)
//        // even though we are using async retrofit API here, we could also use sync
//        // it is just different to show that the callback can be called async.
//        tipidPCApi.getSellers(sellerName).enqueue(
//            object : retrofit2.Callback<FeedItemsResource> {
//                override fun onFailure(call: Call<FeedItemsResource>, t: Throwable) {
//                    // keep a lambda for future retryFeeds
//                    retry = {
//                        loadAfter(params, callback)
//                    }
//                    // publish the error
//                    networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
//                }
//
//                override fun onResponse(
//                    call: Call<FeedItemsResource>,
//                    response: Response<FeedItemsResource>
//                ) {
//
//                    if (response.isSuccessful) {
//                        val items = response.body()?.items ?: emptyList()
//                        Timber.e("loadAfter " + items.size)
//                        // clear retryFeeds since last request succeeded
//                        retry = null
//                        callback.onResult(items)
//                        networkState.postValue(NetworkState.LOADED)
//                    } else {
//                        retry = {
//                            loadAfter(params, callback)
//                        }
//                        networkState.postValue(
//                            NetworkState.error("error code: ${response.code()}")
//                        )
//                    }
//                }
//            })
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<FeedItem>) {
        // ignored, since we only ever append to our initial load
    }

    override fun getKey(item: FeedItem) = item.page.toString()
}