package ariesvelasquez.com.republikapc.repository.seller

import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.network.Resource2
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface ISellerRepository {

    fun getListings(seller: String) : Flow<PagingData<FeedItem>>
    suspend fun isTPCSellerFollowed(seller: String) : Boolean
    suspend fun followTPCSeller(seller: String) : Unit
    suspend fun unfollowTPCSeller(seller: String) : Unit
}