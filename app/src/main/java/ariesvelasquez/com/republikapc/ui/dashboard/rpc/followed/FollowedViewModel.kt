package ariesvelasquez.com.republikapc.ui.dashboard.rpc.followed

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ariesvelasquez.com.republikapc.model.LoadingState
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.repository.dashboard.RPCRepository
import ariesvelasquez.com.republikapc.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class FollowedViewModel
@Inject constructor(
    private val rpcRepository: RPCRepository
) : ViewModel() {

    val followedPagedList = rpcRepository.followedPagedList()
        .cachedIn(viewModelScope + Dispatchers.IO)

    private val _unfollowTask = SingleLiveEvent<ResultState<Unit>>()
    fun unfollowTask() : SingleLiveEvent<ResultState<Unit>> = _unfollowTask
    fun unfollow(name: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    rpcRepository.unfollow(name)
                }
                _unfollowTask.postValue(ResultState.Success(Unit))
            } catch (e: Exception) {
                _unfollowTask.postValue(ResultState.Error(e))
            }
        }
    }
}