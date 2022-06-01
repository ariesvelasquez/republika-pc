package ariesvelasquez.com.republikapc.model.cached_key

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seller_cached_key")
data class SellerCachedKey (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var seller: String,
    var lastUpdated: String
) {
    companion object {
        fun getNewCachedKey(seller: String) : SellerCachedKey {
            return SellerCachedKey(
                seller = seller,
                lastUpdated = System.currentTimeMillis().toString()
            )
        }
    }
}