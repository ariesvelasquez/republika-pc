package ariesvelasquez.com.republikapc.model.rigs

import com.google.gson.annotations.SerializedName

data class Rig(
    @SerializedName("name")
    var name: String = "",
    @SerializedName("id")
    var id: String = "",
    @SerializedName("owner_id")
    var ownerId: String = "",
    @SerializedName("owner_name")
    var ownerName: String = "",
    @SerializedName("owner_thumb")
    var ownerThumb: String = ""
//    @ServerTimestamp
//    var date: Date? = null
)