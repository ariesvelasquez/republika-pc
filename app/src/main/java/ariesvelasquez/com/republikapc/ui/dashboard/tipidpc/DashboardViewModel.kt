package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.repository.dashboard.IDashboardRepository

class DashboardViewModel(private val repository: IDashboardRepository) : ViewModel() {


    private val isInitialized = MutableLiveData<Boolean>()
    private val repoResult = Transformations.map(isInitialized) {
        repository.feeds()
    }
    val items = Transformations.switchMap(repoResult) { it.pagedList }!!
    val networkState = Transformations.switchMap(repoResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showFeedItems() : Boolean {
        this.isInitialized.value = true
        return true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }
}