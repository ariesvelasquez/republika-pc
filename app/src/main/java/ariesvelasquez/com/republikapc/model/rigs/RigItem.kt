package ariesvelasquez.com.republikapc.model.rigs

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class RigItem(
    var name: String? = null,
    var uid: String? = null
//    @ServerTimestamp
//    var date: Date? = null
)