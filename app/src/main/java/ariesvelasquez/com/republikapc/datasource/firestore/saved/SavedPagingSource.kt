package ariesvelasquez.com.republikapc.datasource.firestore.saved

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ariesvelasquez.com.republikapc.model.saved.Saved
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await

@ExperimentalCoroutinesApi
class SavedPagingSource(
    private val savedFirestoreDataSource: Query
) : PagingSource<QuerySnapshot, Saved>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Saved>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Saved> {
        return try {
            val query = savedFirestoreDataSource
            val currentPage = params.key ?: query.get().await()
            val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
            val nextPage = query.startAfter(lastVisibleProduct).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(Saved::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}