package ariesvelasquez.com.republikapc.datasource.firestore.followed

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.datasource.firestore.saved.SavedPagingSource
import ariesvelasquez.com.republikapc.datasource.firestore.saved.followTPCSellerHashMap
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.model.saved.Saved
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ExperimentalCoroutinesApi
@Singleton
class FollowedFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : IFollowedFirestoreService {

    override val firebaseUid: String =
        firebaseAuth.uid ?: ""

    override fun followPagedList(orderBy: String): Flow<PagingData<Saved>> {
        val query: Query = firestore
            .collection(Const.FOLLOWED_COLLECTION)
            .whereEqualTo(Const.OWNER_ID, firebaseUid)
            .orderBy(Const.SELLER)
            .limit(Const.ITEM_PER_PAGE_20)

        return Pager(
            PagingConfig(pageSize = Const.ITEM_PER_PAGE_20.toInt())
        ) { SavedPagingSource(query) }.flow
    }

    override suspend fun unFollow(name: String) {
        return suspendCoroutine { continuation ->
            val combinedUID = firebaseUid + "_" + name
            firestore.collection(Const.FOLLOWED_COLLECTION)
                .document(combinedUID)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun followSeller(name: String) {
        return suspendCoroutine { continuation ->
            val combinedUID = firebaseUid + "_" + name
            firestore.collection(Const.FOLLOWED_COLLECTION)
                .document(combinedUID)
                .set(followTPCSellerHashMap(name, firebaseUid, combinedUID))
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun isTPCUserFollowed(name: String): Boolean {
        return suspendCoroutine { continuation ->
            val combinedDocID = firebaseUid + "_" + name
            firestore.collection(Const.FOLLOWED_COLLECTION)
                .document(combinedDocID)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(snapshot.exists())
                }.addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }
}