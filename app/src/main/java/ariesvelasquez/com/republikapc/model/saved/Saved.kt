package ariesvelasquez.com.republikapc.model.saved

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.Date

//@Entity(tableName = "saved_items")
data class Saved(
//    @PrimaryKey(autoGenerate = true)
//    var id: Int = 0,
    var ownerId: String? = null,
    var date: String? = null,
    var name: String? = null,
    var seller: String? = null,
    var price: String? = null,
    var docId: String? = null,
    var linkId: String? = null,
    @ServerTimestamp
    var createdDate: Date? = null,
    var postDate: String? = null,
    var saveType: String? = null,
    var firstLetter: String? = null) : Serializable {

    var firstLetterIndex : String = ""
    var isVisible: Boolean = true

//    fun mapToObject(hashMap: HashMap<String, Any?>) : Saved {
//        return Saved().apply {
//            ownerId = hashMap[Const.OWNER_ID]?.toString()!!
//            docId = hashMap[Const.DOC_ID]?.toString()!!
//            name = hashMap[Const.NAME]?.toString()!!
//            seller = hashMap[Const.SELLER]?.toString()!!
//            price = hashMap[Const.PRICE]?.toString()!!
//            postDate = hashMap[Const.POST_DATE]?.toString()!!
//            linkId = hashMap[Const.LINK_ID]?.toString()!!
//            createdDate = hashMap[Const.CREATED_DATE]?.toString();
//        }
//    }
}