package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.model.LoadingState
import ariesvelasquez.com.republikapc.model.saved.Saved
import kotlinx.coroutines.flow.Flow

interface IRPCRepository {

    fun savedPagedList() : Flow<PagingData<Saved>>
    suspend fun deleteSavedItem(id: String)
    fun followedPagedList() : Flow<PagingData<Saved>>
    suspend fun follow(name: String)
    suspend fun unfollow(name: String)
}