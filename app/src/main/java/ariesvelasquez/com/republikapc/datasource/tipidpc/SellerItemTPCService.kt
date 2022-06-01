package ariesvelasquez.com.republikapc.datasource.tipidpc

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.api.NewTPCApiService
import ariesvelasquez.com.republikapc.datasource.local.RepublikaPCDatabase
import ariesvelasquez.com.republikapc.datasource.tipidpc.seller.SellerRemoteMediator
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@Singleton
class SellerItemTPCService
@Inject constructor(
    private val tpcService: NewTPCApiService,
    private val database: RepublikaPCDatabase,
) : ISellerItemService {

    override fun pagedSellerItem(seller: String): Flow<PagingData<FeedItem>> {
        val pagingSourceFactory =
            { database.items().sellerItems(seller) }
        return Pager(
            config = PagingConfig(pageSize = 1),
            remoteMediator = SellerRemoteMediator(seller, tpcService, database),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }
}