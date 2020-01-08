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
    @SerializedName("name")
    var name: String = "",
    @SerializedName("seller")
    var seller: String  = "",
    @SerializedName("price")
    var price: String  = "",
    @SerializedName("docId")
    var docId: String = "",
    @SerializedName("link_id")
    var linkId: String = "",
    @SerializedName("date")
    var date: String = "",
    @SerializedName("is_feed")
    var isFeed: Boolean = true,
    @SerializedName("page")
    var page: Int = -1,

    var isLastItem : Boolean = false,
    var isEmptyItem : Boolean = false
) {
    var indexInResponse: Int = -1
}

//    @ColumnInfo(collate = ColumnInfo.NOCASE)
