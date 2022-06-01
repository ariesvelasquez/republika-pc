package ariesvelasquez.com.republikapc.repository.seller

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.datasource.firestore.followed.FollowedFirestoreService
import ariesvelasquez.com.republikapc.datasource.tipidpc.SellerItemTPCService
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@Singleton
class SellerRepository
@Inject constructor(
    private val sellerService: SellerItemTPCService,
    private val followedFirestoreService: FollowedFirestoreService
) : ISellerRepository {

    override fun getListings(seller: String): Flow<PagingData<FeedItem>> {
        return sellerService.pagedSellerItem(seller)
    }

    override suspend fun isTPCSellerFollowed(seller: String): Boolean {
        return followedFirestoreService.isTPCUserFollowed(seller)
    }

    override suspend fun followTPCSeller(seller: String) {
        return followedFirestoreService.followSeller(seller)
    }

    override suspend fun unfollowTPCSeller(seller: String) {
        return followedFirestoreService.unFollow(seller)
    }
}