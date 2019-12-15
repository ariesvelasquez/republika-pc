package ariesvelasquez.com.republikapc.model.rigs

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.util.*

data class Rig(
    @SerializedName("name")
    var name: String = "",
    @SerializedName("id")
    var id: String = "",
    @SerializedName("ownerId")
    var ownerId: String = "",
    @SerializedName("ownerName")
    var ownerName: String = "",
    @SerializedName("ownerThumb")
    var ownerThumb: String = "",
    @SerializedName("itemCount")
    var itemCount: Int = -1,
    @ServerTimestamp
    var date: Date? = null
//    @ServerTimestamp
//    var date: Date? = null
)