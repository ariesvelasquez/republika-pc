package ariesvelasquez.com.republikapc.datasource.firestore.followed

import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.model.saved.Saved
import kotlinx.coroutines.flow.Flow

interface IFollowedFirestoreService {
    val firebaseUid: String
    fun followPagedList(orderBy: String) : Flow<PagingData<Saved>>
    suspend fun unFollow(name: String)
    suspend fun followSeller(name: String)
    suspend fun isTPCUserFollowed(name: String) : Boolean
}