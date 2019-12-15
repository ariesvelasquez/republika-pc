package ariesvelasquez.com.republikapc.model.user

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class User(
    var uid: String? = "",
    var name: String? = "",
    var rigCount: Int = 0,
    var email: String? = "",
    var photoUrl: String? = "",
    @ServerTimestamp
    var createdAt: Date? = null,
    @Exclude
    var isAuthenticated: Boolean = false,
    @Exclude
    var isNew: Boolean = false,
    @Exclude
    var isCreated: Boolean = false
)