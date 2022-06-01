package ariesvelasquez.com.republikapc.utils.extensions

import com.google.firebase.firestore.QuerySnapshot

@Suppress("UNCHECKED_CAST")
inline fun <reified T: Any> getObjects(querySnapshot: QuerySnapshot) : List<T>  {
    return querySnapshot.toObjects(T::class.java)
}