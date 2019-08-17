package ariesvelasquez.com.republikapc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * This inner model is used for converting pojo to database objects
 */
@Entity(tableName = "selling_items")
data class SellingItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @SerializedName("title") var title: String? = ""
)