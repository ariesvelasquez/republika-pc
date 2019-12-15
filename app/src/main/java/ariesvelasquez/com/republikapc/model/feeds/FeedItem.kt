package ariesvelasquez.com.republikapc.model.feeds

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * This inner ariesvelasquez.com.republikapc.model is used for converting pojo to database objects
 */
@Entity(tableName = "feed_items")
data class FeedItem(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("title")
    var title: String = "",
    @SerializedName("seller")
    var seller: String  = "",
    @SerializedName("price")
    var price: String  = "",
    @SerializedName("docId")
    var docId: String = "",
    @SerializedName("page")
    var page: Int = -1
) {
    var indexInResponse: Int = -1
}

//    @ColumnInfo(collate = ColumnInfo.NOCASE)
