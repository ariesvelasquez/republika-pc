package ariesvelasquez.com.republikapc.datasource.tipidpc

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.network.Resource2
import ariesvelasquez.com.republikapc.repository.Listing
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface ISellerItemService {

    fun pagedSellerItem(seller: String): Flow<PagingData<FeedItem>>
}