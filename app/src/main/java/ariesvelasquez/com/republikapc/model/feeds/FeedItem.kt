package ariesvelasquez.com.republikapc.model.feeds

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * This inner model is used for converting pojo to database objects
 */
@Entity(tableName = "feed_items")
data class FeedItem(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("title")
    var title: String,
    @SerializedName("seller")
    val seller: String,
    @SerializedName("page")
    val page: Int
) {
    var indexInResponse: Int = -1
}

//    @ColumnInfo(collate = ColumnInfo.NOCASE)
