package ariesvelasquez.com.republikapc.repository.dashboard.rigitems

import androidx.annotation.MainThread
import androidx.paging.PagedList
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.Const.NAME
import ariesvelasquez.com.republikapc.androidx.PagingRequestHelper
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.utils.createStatusLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import java.io.IOException
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class RigItemsBoundaryCallback (
    private val rigItemDocId: String,
    rigItemsRef: CollectionReference,
    private val handleResponse: (list: List<RigPart>) -> Unit,
    private val ioExecutor: Executor
) : PagedList.BoundaryCallback<RigPart>() {

    private var initialQuery: Query
    private var lastVisible: DocumentSnapshot? = null
    private var lastPageReached: Boolean = false
    private val mItemPerPage = Const.ITEM_PER_PAGE_20

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    init {
        initialQuery = rigItemsRef.document(rigItemDocId)
            .collection(Const.RIGS_ITEM_COLLECTION)
            .orderBy(NAME)
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
                    val rigList = mutableListOf<RigPart>()
                    if (task.isSuccessful) {

                        val querySnapshot = task.result

                        for (document in querySnapshot!!) {
                            val rigItem = document.toObject(RigPart::class.java)
                            rigItem.rigParentId = rigItemDocId
                            rigItem.firstLetterIndex = rigItem.name.first().toUpperCase().toString()
                            rigList.add(rigItem)
                        }

                        // Now get the last item from the list, this will be used as reference
                        // what to fetch next.
                        val querySnapshotSize = querySnapshot.size() - 1
                        if (querySnapshotSize != -1) {
                            lastVisible = querySnapshot.documents[querySnapshotSize]
                        }

                        insertItemsIntoDb(rigList, it)
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
    override fun onItemAtEndLoaded(itemAtEnd: RigPart) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

            // Check if can still fetch more items
            if (!lastPageReached) {
                val nextQuery: Query = initialQuery.startAfter(itemAtEnd)
                try {
                    nextQuery.get().addOnCompleteListener { task ->
                        val nextRigList = mutableListOf<RigPart>()
                        if (task.isSuccessful) {
                            val querySnapshot = task.result
                            for (document in querySnapshot!!) {
                                val rigItem = document.toObject(RigPart::class.java)
                                rigItem.rigParentId = rigItemDocId
                                rigItem.firstLetterIndex = rigItem.name.first().toUpperCase().toString()
                                nextRigList.add(rigItem)
                            }

                            if (nextRigList.size < mItemPerPage) {
                                lastPageReached = true
                            } else {
                                lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                            }

                            insertItemsIntoDb(nextRigList, it)
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

    override fun onItemAtFrontLoaded(itemAtFront: RigPart) {
        // ignored, since we only ever append to what's in the DB
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        list: List<RigPart>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(list)
            it.recordSuccess()
        }
    }
}