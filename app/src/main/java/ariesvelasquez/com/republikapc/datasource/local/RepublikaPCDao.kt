package ariesvelasquez.com.republikapc.datasource.local

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.*
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigparts.RigPart

@Dao
interface RepublikaPCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeeds(items: List<FeedItem>)

    /*
     * Fetch all items from feed_items
     */
    @Query("SELECT * FROM feed_items WHERE isFeed = 1 ORDER BY indexInResponse ASC ")
    fun feedItems(): DataSource.Factory<Int, FeedItem>

    @Query("SELECT * FROM feed_items WHERE isFeed = 0 AND seller = :sellerName ORDER BY name ASC")
    fun sellerItems(sellerName: String): PagingSource<Int, FeedItem>

    @Query("SELECT * FROM feed_items WHERE isFeed = 0 AND seller = :sellerName ORDER BY name ASC")
    fun sellerItemsLiveData(sellerName: String): LiveData<List<FeedItem>>

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


    /*
     * Saved
     */
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertSaved(items: List<Saved>)

    /*
     * Fetch all items from saved_items
     */
//    @Query("SELECT * FROM saved_items ORDER BY firstLetterIndex ASC")
//    fun savedItems(): DataSource.Factory<Int, Saved>
//
//    @Query("DELETE FROM saved_items")
//    fun nukeSavedItems()
//
//    @Query("DELETE FROM saved_items WHERE docId = :docId")
//    fun removeItem(docId: String)
//
//    @Insert
//    fun insertSavedItem(saved: Saved)


    /*
     * Fetch all items from rig_parts
     */
    @Query("SELECT * FROM rig_parts WHERE rigParentId = :rigId ORDER BY firstLetterIndex ASC")
    fun rigParts(rigId: String): DataSource.Factory<Int, RigPart>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRigParts(items: List<RigPart>)

    @Insert
    fun insertRigPart(rigPart: RigPart)

    @Query("DELETE FROM rig_parts WHERE docId = :partId AND rigParentId = :rigId")
    fun removeRigPart(rigId: String, partId: String)

    @Query("DELETE FROM rig_parts WHERE rigParentId = :rigId")
    fun nukeRigParts(rigId: String)

    @Query("DELETE FROM rig_parts")
    fun nukeAllRigParts()
}