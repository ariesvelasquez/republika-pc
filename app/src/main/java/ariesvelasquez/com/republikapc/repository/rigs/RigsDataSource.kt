package ariesvelasquez.com.republikapc.repository.rigs

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import ariesvelasquez.com.republikapc.Const.OWNER_ID
import ariesvelasquez.com.republikapc.Const.RIGS_ITEM_PER_PAGE
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import timber.log.Timber
import java.io.IOException

class RigsDataSource(rigsReference: CollectionReference) : ItemKeyedDataSource<String, Rig>() {

    private var initialQuery: Query
    private var lastVisible: DocumentSnapshot? = null
    private var lastPageReached: Boolean = false
    private var pageNumber = 1

    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        initialQuery = rigsReference.
            whereEqualTo(OWNER_ID, firebaseUser?.uid).
            limit(RIGS_ITEM_PER_PAGE)
//        initialQuery = rigsReference.orderBy("name", Query.Direction.ASCENDING).limit(RIGS_ITEM_PER_PAGE)
    }

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter and we don't support loadBefore
     * in this example.
     * <p>
     * See BoundaryCallback example for a more complete example on syncing multiple network states.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<Rig>
    ) {
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        Timber.e("loadInitial")
        try {
            initialQuery.get().addOnCompleteListener { task ->
                val rigList = mutableListOf<Rig>()
                if (task.isSuccessful) {
                    Timber.e("task.isSuccessful")

                    val querySnapshot = task.result
                    Timber.e("querySnapshot size " + querySnapshot?.size())

                    for (document in querySnapshot!!) {
                        val rigItem = document.toObject(Rig::class.java)
                        rigList.add(rigItem)
                    }

                    // Return the collected list from firestore
                    Timber.e("Riglist size = " + rigList.size)
                    callback.onResult(rigList)
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)

                    // Now get the last item from the list, this will be used as reference
                    // what to fetch next.
                    val querySnapshotSize = querySnapshot.size() - 1
                    if (querySnapshotSize != -1) {
                        Timber.e("has last item")
                        lastVisible = querySnapshot.documents[querySnapshotSize]
                    }
                } else {
                    val error = NetworkState.error(task.exception?.message!!)
                    networkState.postValue(error)
                    initialLoad.postValue(NetworkState.LOADED)
                    Timber.e(task.exception?.message!!)
                }
            }
        } catch (ioException: IOException) {
            val error = NetworkState.error(ioException.message ?: "unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Rig>) {
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)

        val nextQuery: Query = initialQuery.startAfter(lastVisible!!)
        nextQuery.get().addOnCompleteListener { task ->
            val nextRigList = mutableListOf<Rig>()
            if (task.isSuccessful) {
                val querySnapshot = task.result
                if (!lastPageReached) {
                    for (document in querySnapshot!!) {
                        val rigItem = document.toObject(Rig::class.java)
                        nextRigList.add(rigItem)
                    }
                    callback.onResult(nextRigList)
                    pageNumber++

                    // set network value to loading.
                    networkState.postValue(NetworkState.LOADED)

                    if (nextRigList.size < RIGS_ITEM_PER_PAGE) {
                        lastPageReached = true
                    } else {
                        lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                    }
                }
            } else {
                val error = NetworkState.error(task.exception?.message!!)
                networkState.postValue(error)
                initialLoad.postValue(NetworkState.LOADED)
                Timber.e(task.exception?.message!!)
            }
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Rig>) {

    }

    override fun getKey(item: Rig): String = "something"
}