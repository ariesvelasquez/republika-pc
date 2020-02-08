package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.api.TipidPCApi
import ariesvelasquez.com.republikapc.androidx.PagingRequestHelper
import ariesvelasquez.com.republikapc.model.error.Error
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.utils.createStatusLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class SavedBoundaryCallback (
    private val savedReference: CollectionReference,
    private val handleResponse: (list: List<Saved>) -> Unit,
    private val ioExecutor: Executor
) : PagedList.BoundaryCallback<Saved>() {

    private var initialQuery: Query
    private var lastVisible: DocumentSnapshot? = null
    private var lastPageReached: Boolean = false
    private val mItemPerPage = Const.ITEM_PER_PAGE_20

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()
    val isEmpty = MutableLiveData<Boolean>()

    init {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        initialQuery = savedReference
            .whereEqualTo(Const.OWNER_ID, firebaseUser?.uid)
            .orderBy("name")
            .limit(mItemPerPage)
    }

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {

            try {
                initialQuery.get().addOnCompleteListener { task ->
                    val savedList = mutableListOf<Saved>()
                    if (task.isSuccessful) {

                        val querySnapshot = task.result

                        for (document in querySnapshot!!) {
                            val savedItem = document.toObject(Saved::class.java)
                            savedList.add(savedItem)
                        }
                        Timber.e("onZeroItemsLoaded > isEmpty " + savedList.isEmpty())
                        isEmpty.value = (savedList.isEmpty())

                        // Now get the last item from the list, this will be used as reference
                        // what to fetch next.
                        val querySnapshotSize = querySnapshot.size() - 1
                        if (querySnapshotSize != -1) {
                            lastVisible = querySnapshot.documents[querySnapshotSize]
                        }

                        insertItemsIntoDb(savedList, it)
                    } else {
                        it.recordFailure(Throwable(task.exception?.message!!))
                    }
                }
            } catch (ioException: IOException) {
                it.recordFailure(Throwable(ioException.message ?: "unknown error"))
            }
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Saved) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

            // Check if can still fetch more items
            if (!lastPageReached) {
                val nextQuery: Query = initialQuery.startAfter(itemAtEnd)
                try {
                    nextQuery.get().addOnCompleteListener { task ->
                        val nextSavedList = mutableListOf<Saved>()
                        if (task.isSuccessful) {
                            val querySnapshot = task.result
                            for (document in querySnapshot!!) {
                                val savedItem = document.toObject(Saved::class.java)
                                nextSavedList.add(savedItem)
                            }

                            if (nextSavedList.size < mItemPerPage) {
                                lastPageReached = true
                            } else {
                                lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                            }

                            insertItemsIntoDb(nextSavedList, it)

                        } else {
                            it.recordFailure(Throwable(task.exception?.message!!))
                        }
                    }
                } catch (ioException: IOException) {
                    it.recordFailure(Throwable(ioException.message ?: "unknown error"))
                }
            } else {
                it.recordSuccess()
            }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Saved) {
        // ignored, since we only ever append to what's in the DB
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        list: List<Saved>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(list)
            it.recordSuccess()
        }
    }
}