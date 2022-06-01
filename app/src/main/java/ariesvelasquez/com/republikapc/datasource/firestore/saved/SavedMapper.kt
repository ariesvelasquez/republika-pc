package ariesvelasquez.com.republikapc.datasource.firestore.saved

import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.utils.Tools
import com.google.firebase.firestore.FieldValue

fun FeedItem.saveFeedHashMap(userId: String, docReference: String): HashMap<String, Any?> {
    val saveItemMap = hashMapOf<String, Any?>()
    saveItemMap[Const.DOC_ID] = docReference
    saveItemMap[Const.OWNER_ID] = userId
    saveItemMap[Const.NAME] = this.name
    saveItemMap[Const.SELLER] = this.seller
    saveItemMap[Const.PRICE] = Tools.formatPrice(this.price)
    saveItemMap[Const.POST_DATE] = this.date
    saveItemMap[Const.LINK_ID] = this.linkId
    saveItemMap[Const.CREATED_DATE] = FieldValue.serverTimestamp()
    saveItemMap[Const.TYPE] = Const.SaveType.TPC_ITEM
    return saveItemMap
}

fun followTPCSellerHashMap(name: String, userId: String, docReference: String): HashMap<String, Any?> {
    val followItemMap = hashMapOf<String, Any?>()
    followItemMap[Const.OWNER_ID] = userId
    followItemMap[Const.SELLER] = name
    followItemMap[Const.DOC_ID] = docReference
    followItemMap[Const.FIRST_LETTER] = name.first().toUpperCase().toString()
    followItemMap[Const.CREATED_DATE] = FieldValue.serverTimestamp()
    followItemMap[Const.TYPE] = Const.SaveType.TPC_ITEM
    return followItemMap
}