package ariesvelasquez.com.republikapc.datasource.local

import androidx.room.*
import ariesvelasquez.com.republikapc.model.cached_key.SellerCachedKey
import timber.log.Timber

@Dao
interface SellerCachedKeyDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sellerCached: SellerCachedKey)

    @Query("Update seller_cached_key SET lastUpdated = :lastUpdated")
    suspend fun updateCachedKey(lastUpdated: String)

    @Query("SELECT * FROM seller_cached_key WHERE seller = :seller")
    suspend fun cachedKeyBySeller(seller: String): SellerCachedKey?

    @Query("SELECT * FROM seller_cached_key WHERE seller = :seller")
    suspend fun getAllBySeller(seller: String) : List<SellerCachedKey>

    suspend fun insertOrUpdate(sellerCached: SellerCachedKey) {
        val sellercachedList = getAllBySeller(sellerCached.seller)
        if (sellercachedList.isEmpty()) {
            Timber.e("Empty Seller cached, inserted new")
            insert(sellerCached)
        } else {
            Timber.e("Existing Seller cached, updated new")
            updateCachedKey(sellerCached.lastUpdated)
        }
    }
}