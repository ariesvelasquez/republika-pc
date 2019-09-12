package ariesvelasquez.com.republikapc.repository.search

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import java.util.concurrent.Executor

class SearchSourceFactory(
    val tipidPCApi: TipidPCApi,
    val searchVal: String,
    val networkExecutor: Executor
) : DataSource.Factory<String, FeedItem>() {

    val sourceLiveData = MutableLiveData<SearchDataSource>()

    override fun create(): DataSource<String, FeedItem> {
        val source = SearchDataSource(tipidPCApi, searchVal, networkExecutor)
        sourceLiveData.postValue(source)
        return source
    }
}