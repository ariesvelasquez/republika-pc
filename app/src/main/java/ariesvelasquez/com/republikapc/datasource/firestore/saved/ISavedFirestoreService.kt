package ariesvelasquez.com.republikapc.datasource.firestore.saved

import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.saved.Saved
import kotlinx.coroutines.flow.Flow

interface ISavedFirestoreService {
    val firestoreUserId: String
    fun savedPagedList() : Flow<PagingData<Saved>>
    suspend fun deleteSavedItem(id: String)
    suspend fun saveFeedItem(feedItem: FeedItem)
}