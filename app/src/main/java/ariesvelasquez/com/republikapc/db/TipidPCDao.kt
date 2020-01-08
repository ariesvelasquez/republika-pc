package ariesvelasquez.com.republikapc.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ariesvelasquez.com.republikapc.model.feeds.FeedItem

@Dao
interface TipidPCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<FeedItem>)

    /*
     * Fetch all items from feed_items
     */
    @Query("SELECT * FROM feed_items WHERE isFeed = 1 ORDER BY indexInResponse ASC ")
    fun feedItems(): DataSource.Factory<Int, FeedItem>

    @Query("SELECT * FROM feed_items WHERE isFeed = 0 AND seller = :sellerName ORDER BY name ASC")
    fun sellerItems(sellerName: String): DataSource.Factory<Int, FeedItem>

    /*
     * Get items for specific seller from the seller_items table
     */
//    @Query("SELECT * FROM feed_items WHERE seller = :seller ORDER BY indexInResponse ASC")
//    fun itemsBySeller(seller: String): DataSource.Factory<Int, FeedItem>

//    @Query("DELETE FROM selling_items WHERE seller = :seller")
//    fun deleteBySeller(seller: String)

    @Query("DELETE FROM feed_items WHERE isFeed = 1")
    fun nukeFeedItems()

    @Query("DELETE FROM feed_items WHERE isFeed = 0 AND seller = :sellerName")
    fun deleteOldSellerItems(sellerName: String)
}