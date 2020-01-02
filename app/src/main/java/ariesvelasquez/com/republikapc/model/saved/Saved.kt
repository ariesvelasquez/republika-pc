package ariesvelasquez.com.republikapc.model.saved

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Saved(
    var ownerId: String = "",
    @ServerTimestamp
    var date: Date? = null,
    var name: String = "",
    var seller: String  = "",
    var price: String  = "",
    var docId: String = "",
    var linkId: String = "",
    var postDate: String = ""
)