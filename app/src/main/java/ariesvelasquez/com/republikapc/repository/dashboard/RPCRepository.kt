package ariesvelasquez.com.republikapc.repository.dashboard

import androidx.paging.PagingData
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.datasource.firestore.followed.FollowedFirestoreService
import ariesvelasquez.com.republikapc.datasource.firestore.saved.SavedFirestoreService
import ariesvelasquez.com.republikapc.model.saved.Saved
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Singleton
class RPCRepository
@Inject constructor(
    private val savedFirestoreService: SavedFirestoreService,
    private val followedFirestoreService: FollowedFirestoreService
) : IRPCRepository {

    override fun savedPagedList(): Flow<PagingData<Saved>> {
        return savedFirestoreService.savedPagedList()
    }

    override suspend fun deleteSavedItem(id: String) {
        return savedFirestoreService.deleteSavedItem(id)
    }

    override fun followedPagedList(): Flow<PagingData<Saved>> {
        return followedFirestoreService.followPagedList(Const.NAME)
    }

    override suspend fun follow(name: String) {
        return followedFirestoreService.followSeller(name)
    }

    override suspend fun unfollow(name: String) {
        return followedFirestoreService.unFollow(name)
    }
}