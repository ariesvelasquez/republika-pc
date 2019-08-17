package ariesvelasquez.com.republikapc.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ariesvelasquez.com.republikapc.model.SellingItem

@Dao
interface TipidPCDao {

    @Insert
    fun insertSellingItems(sellingItemsResource: List<SellingItem>) : List<Long>

    /**
     * Get all the selling items from database
     */
    @Query("SELECT * FROM selling_items")
    fun getAllSellingItems(): LiveData<List<SellingItem>>
}