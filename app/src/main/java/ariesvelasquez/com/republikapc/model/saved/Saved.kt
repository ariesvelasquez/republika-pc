package ariesvelasquez.com.republikapc.model.saved

import androidx.room.Entity
import androidx.room.PrimaryKey
import ariesvelasquez.com.republikapc.Const

@Entity(tableName = "saved_items")
data class Saved(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var ownerId: String = "",
    var date: String? = "",
    var name: String = "",
    var seller: String  = "",
    var price: String  = "",
    var docId: String = "",
    var linkId: String = "",
    var postDate: String = "") {

    var firstLetterIndex : String = ""

    fun mapToObject(hashMap: HashMap<String, Any?>) : Saved {
        return Saved().apply {
            ownerId = hashMap[Const.OWNER_ID]?.toString()!!
            docId = hashMap[Const.DOC_ID]?.toString()!!
            name = hashMap[Const.NAME]?.toString()!!
            seller = hashMap[Const.SELLER]?.toString()!!
            price = hashMap[Const.PRICE]?.toString()!!
            postDate = hashMap[Const.POST_DATE]?.toString()!!
            linkId = hashMap[Const.LINK_ID]?.toString()!!
        }
    }
}