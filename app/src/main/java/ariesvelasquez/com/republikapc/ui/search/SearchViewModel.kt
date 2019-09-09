package ariesvelasquez.com.republikapc.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.repository.tipidpc.search.ISearchRepository

class SearchViewModel(private val repository: ISearchRepository) : ViewModel() {

    private val searchVal = MutableLiveData<String>()
    private val repoResult = map(searchVal) {
        repository.searchItems(it)
    }
    val items = Transformations.switchMap(repoResult) { it.pagedList }!!
    val networkState = Transformations.switchMap(repoResult) { it.networkState }!!
    val refreshState = Transformations.switchMap(repoResult) { it.refreshState }!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun searchItems(searchVal: String) : Boolean {
        if (this.searchVal.value == searchVal) {
            return false
        }
        this.searchVal.value = searchVal
        return true
    }

//    fun showSeller(seller: String): Boolean {
//        if (this.seller.value == seller) {
//            return false
//        }
//        this.seller.value = seller
//        return true
//    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }
}