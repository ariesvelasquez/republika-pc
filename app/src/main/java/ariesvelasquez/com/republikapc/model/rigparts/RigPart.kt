package ariesvelasquez.com.republikapc.model.rigparts

import androidx.room.Entity
import androidx.room.PrimaryKey
import ariesvelasquez.com.republikapc.Const

/**
 * This inner ariesvelasquez.com.republikapc.model is used for converting pojo to database objects
 */
@Entity(tableName = "rig_parts")
data class RigPart(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var seller: String  = "",
    var price: String  = "",
    var docId: String = "",
    var linkId: String = "",
    var date: String = "",

    var isLastItem : Boolean = false,
    var isEmptyItem : Boolean = false
) {
    var firstLetterIndex: String = ""
    var rigParentId: String = ""

    fun mapToObject(hashMap: HashMap<String, Any?>) : RigPart {
        return RigPart().apply {
            name = hashMap[Const.NAME]?.toString()!!
            seller = hashMap[Const.SELLER]?.toString()!!
            price = hashMap[Const.PRICE]?.toString()!!
            docId = hashMap[Const.DOC_ID]?.toString()!!
            linkId = hashMap[Const.LINK_ID]?.toString()!!
            date = hashMap[Const.POST_DATE]?.toString()!!
        }
    }
}

