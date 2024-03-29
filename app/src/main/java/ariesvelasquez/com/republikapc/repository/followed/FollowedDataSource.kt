package ariesvelasquez.com.republikapc.repository.followed

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.Const.FIRST_LETTER
import ariesvelasquez.com.republikapc.Const.ITEM_PER_PAGE_20
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import timber.log.Timber
import java.io.IOException

class FollowedDataSource(followedReference: CollectionReference) : ItemKeyedDataSource<String, Saved>() {

    private var initialQuery: Query
    private var lastVisible: DocumentSnapshot? = null
    private var lastPageReached: Boolean = false
    private val mItemPerPage = ITEM_PER_PAGE_20

    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        initialQuery = followedReference
            .whereEqualTo(Const.OWNER_ID, firebaseUser?.uid)
            .orderBy(FIRST_LETTER)
            .limit(mItemPerPage)
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
        callback: LoadInitialCallback<Saved>
    ) {
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)

        try {
            initialQuery.get().addOnCompleteListener { task ->
                val followedList = mutableListOf<Saved>()
                if (task.isSuccessful) {

                    val querySnapshot = task.result

                    for (document in querySnapshot!!) {
                        val savedItem = document.toObject(Saved::class.java)
                        followedList.add(savedItem)
                    }
                    Timber.e("followedList size " + followedList.size)

                    // Return the collected list from firestore
                    callback.onResult(followedList)
                    networkState.postValue(NetworkState.LOADED)
                    initialLoad.postValue(NetworkState.LOADED)

                    // Now get the last item from the list, this will be used as reference
                    // what to fetch next.
                    val querySnapshotSize = querySnapshot.size() - 1
                    if (querySnapshotSize != -1) {
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
            val error = NetworkState.error(ioException.message ?: "followed unknown error")
            networkState.postValue(error)
            initialLoad.postValue(error)
        }

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Saved>) {
        // set network value to loading.
        networkState.postValue(NetworkState.LOADING)

        // Check if can still fetch more items
        if (!lastPageReached) {
            val nextQuery: Query = initialQuery.startAfter(lastVisible!!)
            try {
                nextQuery.get().addOnCompleteListener { task ->
                    val nextFollowedList = mutableListOf<Saved>()
                    if (task.isSuccessful) {
                        val querySnapshot = task.result
                        for (document in querySnapshot!!) {
                            val savedItem = document.toObject(Saved::class.java)
                            nextFollowedList.add(savedItem)
                        }
                        callback.onResult(nextFollowedList)

                        // set network value to loading.
                        networkState.postValue(NetworkState.LOADED)
                        initialLoad.postValue(NetworkState.LOADED)

                        if (nextFollowedList.size < mItemPerPage) {
                            lastPageReached = true
                        } else {
                            lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
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
        } else {
            networkState.postValue(NetworkState.LOADED)
        }
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Saved>) {

    }

    override fun getKey(item: Saved): String = "something"
}