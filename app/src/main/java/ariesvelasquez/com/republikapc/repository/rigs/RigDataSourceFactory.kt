package ariesvelasquez.com.republikapc.repository.rigs

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.model.rigs.Rig
import com.google.firebase.firestore.CollectionReference

class RigDataSourceFactory(private val rigReference: CollectionReference) : DataSource.Factory<String, Rig>(){

    val sourceLiveData = MutableLiveData<RigsDataSource>()
    private val searchText: String? = null

    override fun create(): DataSource<String, Rig> {
        val source = RigsDataSource(rigReference)
        sourceLiveData.postValue(source)
        return source
    }
}