package ariesvelasquez.com.republikapc.repository.search


import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import java.util.concurrent.Executor

class SearchSellerSourceFactory(
    val tipidPCApi: TipidPCApi,
    val sellerName: String,
    val networkExecutor: Executor
) : DataSource.Factory<String, FeedItem>() {

    val sourceLiveData = MutableLiveData<SearchSellerDataSource>()

    override fun create(): DataSource<String, FeedItem> {
        val source = SearchSellerDataSource(tipidPCApi, sellerName, networkExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}