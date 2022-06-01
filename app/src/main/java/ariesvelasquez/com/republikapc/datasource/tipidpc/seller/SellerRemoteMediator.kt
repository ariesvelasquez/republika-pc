package ariesvelasquez.com.republikapc.datasource.tipidpc.seller

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import ariesvelasquez.com.republikapc.api.NewTPCApiService
import ariesvelasquez.com.republikapc.datasource.local.RepublikaPCDatabase
import ariesvelasquez.com.republikapc.model.cached_key.SellerCachedKey
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.utils.Tools
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalPagingApi
@Singleton
class SellerRemoteMediator
@Inject
constructor(
    private val seller: String,
    private val apiService: NewTPCApiService,
    private val database: RepublikaPCDatabase
): RemoteMediator<Int, FeedItem>() {


    override suspend fun initialize(): InitializeAction {
        val cachedSellerItems: SellerCachedKey? = database.sellerCachedKeyDao().cachedKeyBySeller(seller)
        Timber.e("cachedSellerItems " + cachedSellerItems)

        // Todo; Add validation if the cachedSellerItems is cached long time ago
        return if (cachedSellerItems?.lastUpdated == null) {
            Timber.e("Not Skipped")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            Timber.e("Skipped")
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, FeedItem>
    ): MediatorResult {
        Timber.e("SellerRemoteMediator Loadtype %s", loadType)

        return try {
            if (loadType == LoadType.PREPEND || loadType == LoadType.APPEND)
                return MediatorResult.Success(true)

            val cachedSellerItems = database.sellerCachedKeyDao().cachedKeyBySeller(seller)
            val sellerItemSuccessResponse = apiService.getSellerItems(seller).body()

            if (cachedSellerItems == null || loadType == LoadType.REFRESH) {
                refreshSellerCachedKey()
            }

            database.runInTransaction {
                if (loadType == LoadType.REFRESH) {
                    Timber.e("Deleted Old Items of $seller")
                    database.items().deleteOldSellerItems(seller)
                }

                var itemsWithIndex = sellerItemSuccessResponse?.items ?: emptyList()
                sellerItemSuccessResponse?.items.let {
                    itemsWithIndex = it?.mapIndexed { index, child ->
                        child.lastRefresh = Date().toString()
                        child.indexInResponse = child.page
                        child.totalItems = it.size
                        child
                    }!!
                }

                database.items().insertFeeds(itemsWithIndex).run {
                    Timber.e("Inserted Seller: $seller Items: ${itemsWithIndex.size}}")
                }
            }

            MediatorResult.Success(true)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun refreshSellerCachedKey() {
        database.sellerCachedKeyDao()
            .insertOrUpdate(SellerCachedKey.getNewCachedKey(seller))
    }
}