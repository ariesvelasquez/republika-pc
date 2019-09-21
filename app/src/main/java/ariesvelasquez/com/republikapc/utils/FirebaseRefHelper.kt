package ariesvelasquez.com.republikapc.utils

import ariesvelasquez.com.republikapc.Const
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


class FirebaseHelper {

    fun provideUserRef(firebaseDb: FirebaseFirestore): CollectionReference {
        return FirebaseFirestore.getInstance().collection(Const.USERS_COLLECTION)
    }
}