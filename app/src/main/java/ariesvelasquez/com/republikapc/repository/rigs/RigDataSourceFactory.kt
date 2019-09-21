package ariesvelasquez.com.republikapc.repository.rigs

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import ariesvelasquez.com.republikapc.model.rigs.RigItem
import com.google.firebase.firestore.CollectionReference

class RigDataSourceFactory(private val rigReference: CollectionReference) : DataSource.Factory<String, RigItem>(){

    val sourceLiveData = MutableLiveData<RigsDataSource>()
    private val searchText: String? = null

    override fun create(): DataSource<String, RigItem> {
        val source = RigsDataSource(rigReference)
        sourceLiveData.postValue(source)
        return source
    }
}