package ariesvelasquez.com.republikapc.repository.rigs

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import com.google.firebase.firestore.CollectionReference

class RigItemsDataSourceFactory(private val rigReference: CollectionReference, private val rigItemId: String) : DataSource.Factory<String, FeedItem>(){

    val sourceLiveData = MutableLiveData<RigItemsDataSource>()
    private val searchText: String? = null

    override fun create(): DataSource<String, FeedItem> {
        val source = RigItemsDataSource(rigReference, rigItemId)
        sourceLiveData.postValue(source)
        return source
    }
}