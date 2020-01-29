package ariesvelasquez.com.republikapc.repository.followed

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.model.saved.Saved
import com.google.firebase.firestore.CollectionReference


class FollowedDataSourceFactory(private val followedReference: CollectionReference) : DataSource.Factory<String, Saved>(){

    val sourceLiveData = MutableLiveData<FollowedDataSource>()
    private val searchText: String? = null

    override fun create(): DataSource<String, Saved> {
        val source = FollowedDataSource(followedReference = followedReference)
        sourceLiveData.postValue(source)
        return source
    }
}