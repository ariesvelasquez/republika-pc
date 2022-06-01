package ariesvelasquez.com.republikapc.model.feeds

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import ariesvelasquez.com.republikapc.model.saved.Saved
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
    var name: String? = null,
    @SerializedName("seller")
    var seller: String? = null,
    @SerializedName("price")
    var price: String? = null,
    @SerializedName("docId")
    var docId: String? = null,
    @SerializedName("link_id")
    var linkId: String? = null,
    @SerializedName("date")
    var date: String? = null,
    @SerializedName("is_feed")
    var isFeed: Boolean? = null,
    @SerializedName("page")
    var page: Int = -1,

    var isLastItem : Boolean = false,
    var isEmptyItem : Boolean = false
) {
    var indexInResponse: Int = -1
    var lastRefresh: String = ""
    var totalItems: Int = 0

    companion object {

        class DiffCallback : DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                return oldItem.docId == newItem.docId
            }

            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}