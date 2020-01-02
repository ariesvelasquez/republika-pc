package ariesvelasquez.com.republikapc.repository.saved

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import ariesvelasquez.com.republikapc.model.saved.Saved
import com.google.firebase.firestore.CollectionReference


class SavedDataSourceFactory(private val savedReference: CollectionReference) : DataSource.Factory<String, Saved>(){

    val sourceLiveData = MutableLiveData<SavedDataSource>()
    private val searchText: String? = null

    override fun create(): DataSource<String, Saved> {
        val source = SavedDataSource(savedReference = savedReference)
        sourceLiveData.postValue(source)
        return source
    }
}