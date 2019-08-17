package ariesvelasquez.com.republikapc.model

import com.google.gson.annotations.SerializedName

data class SellingItemResource (
    @SerializedName("title") var title: String? = ""
)