package ariesvelasquez.com.republikapc.datasource.firestore.saved

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.Const.SAVED_COLLECTION
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
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
class SavedFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ISavedFirestoreService {

    override val firestoreUserId: String
        get() = firebaseAuth.uid ?: ""

    override fun savedPagedList(): Flow<PagingData<Saved>> {
        val query: Query = firestore
            .collection(SAVED_COLLECTION)
            .whereEqualTo(Const.OWNER_ID, firebaseAuth.currentUser?.uid)
            .orderBy(Const.NAME)
            .limit(Const.ITEM_PER_PAGE_10)

        return Pager(
            PagingConfig(pageSize = Const.ITEM_PER_PAGE_20.toInt())
        ) { SavedPagingSource(query) }.flow
    }

    override suspend fun deleteSavedItem(id: String) {
        return suspendCoroutine { continuation ->
            firestore.collection(SAVED_COLLECTION)
                .document(id)
                .delete()
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun saveFeedItem(feedItem: FeedItem) {
        return suspendCoroutine { continuation ->
            val newSavedDocumentReference = firestore.collection(SAVED_COLLECTION).document()
            newSavedDocumentReference.set(
                feedItem.saveFeedHashMap(
                    firestoreUserId,
                    newSavedDocumentReference.id
                )
            ).addOnSuccessListener {
                continuation.resume(Unit)
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
        }
    }
}