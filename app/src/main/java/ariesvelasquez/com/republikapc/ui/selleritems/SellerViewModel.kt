package ariesvelasquez.com.republikapc.ui.selleritems

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.cachedIn
import ariesvelasquez.com.republikapc.model.LoadState
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.repository.seller.SellerRepository
import ariesvelasquez.com.republikapc.ui.base.BaseViewModel
import ariesvelasquez.com.republikapc.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@HiltViewModel
class SellerViewModel
@Inject constructor(
    private val sellerRepository: SellerRepository,
) : BaseViewModel() {

    private var _isSyncing = MutableLiveData<Boolean>()
    val isSyncing : LiveData<Boolean> get() = _isSyncing

    private var _actionState = MutableLiveData<SellerActionsState>()
    val actionState : LiveData<SellerActionsState> get() = _actionState

    private var isFollowed: Boolean = false
    private var _isTPCSellerFollowed = SingleLiveEvent<Result<Boolean>>()
    val isTPCSellerFollowed : SingleLiveEvent<Result<Boolean>> get() = _isTPCSellerFollowed

    init {
        _actionState.postValue(SellerActionsState(isSyncing = false, followed = false))
    }

    fun sellerItemsPagedList(seller: String) = sellerRepository.getListings(seller = seller)
        .cachedIn(viewModelScope + Dispatchers.IO)

    fun updateSyncState(isSyncing: Boolean) {
        _isSyncing.postValue(isSyncing)
        _actionState.postValue(
            _actionState.value.also {
                it?.isSyncing = isSyncing
            }
        )
    }

    fun checkIfTPCSellerIsFollowed(seller: String) {
        viewModelScope.launch {
            try {
                _loadState.postValue(LoadState.Loading(CHECK_TPC_FOLLOWED_TASK))
                withContext(Dispatchers.IO) {
                    isFollowed = sellerRepository.isTPCSellerFollowed(seller)
                }
                _isTPCSellerFollowed.postValue(Result.success(isFollowed))
            } catch (e: Exception) {
                _loadState.postValue(LoadState.Error(e, CHECK_TPC_FOLLOWED_TASK))
            } finally {
                _loadState.postValue(LoadState.Loaded(CHECK_TPC_FOLLOWED_TASK))
            }
        }
    }

    fun followUnfollowTPCSeller(seller: String) {
        viewModelScope.launch {
            try {
                _loadState.postValue(LoadState.Loading(FOLLOW_UNFOLLOW_TASK))
                withContext(Dispatchers.IO) {
                    if (isFollowed) {
                        sellerRepository.unfollowTPCSeller(seller)
                    } else {
                        sellerRepository.followTPCSeller(seller)
                    }
                }
                _loadState.postValue(LoadState.Loaded(FOLLOW_UNFOLLOW_TASK))
            } catch (e: Exception) {
                _loadState.postValue(LoadState.Error(e, FOLLOW_UNFOLLOW_TASK))
            }
        }
    }

    companion object LoadType {
        const val CHECK_TPC_FOLLOWED_TASK = "CHECK_TPC_FOLLOWED_TASK"
        const val FOLLOW_UNFOLLOW_TASK = "FOLLOW_UNFOLLOW_TASK"
    }
}