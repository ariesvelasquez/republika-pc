package ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved

import androidx.lifecycle.*
import androidx.paging.cachedIn
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.repository.dashboard.RPCRepository
import ariesvelasquez.com.republikapc.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.lang.Exception
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SavedViewModel
@Inject
constructor(
    private val rpcRepository: RPCRepository,
) : ViewModel() {

    private val deleteTask = SingleLiveEvent<ResultState<Unit>>()
    fun deleteTask() : SingleLiveEvent<ResultState<Unit>> = deleteTask

    val savedPagedList = rpcRepository.savedPagedList()
        .cachedIn(viewModelScope + Dispatchers.IO)

    fun deleteSaved(id: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    rpcRepository.deleteSavedItem(id)
                }
                deleteTask.postValue(ResultState.Success(Unit))
            } catch (e: Exception) {
                deleteTask.postValue(ResultState.Error(e))
            }
        }
    }
}