package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.repository.tipidpc.feeds.IFeedsRepository

class FeedsViewModel(private val repository: IFeedsRepository) : ViewModel() {


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

//    fun currentSeller(): String? = this.seller.value

//    /**
//     * The collection of the items that is observed by the UI
//     */
//    private val pageNumberLiveData = MutableLiveData<Int>()
//
//    // Define the repository
//    private var tipidPcRepositoryNBR: TipidPCRepositoryNBR =
//        TipidPCRepositoryNBR(
//            TipidPCDatabase.getDatabase(application).tipidPCDao(),
//            TipidPcApiService.getTPCApiService()
//        )
//
//    fun sellingItems() : LiveData<Resource<List<FeedItem>?>> = Transformations.switchMap(pageNumberLiveData, ::testFunc)
//
//    private fun testFunc(pageNumber: Int) : LiveData<Resource<List<FeedItem>?>> = tipidPcRepositoryNBR.getTPCSellingItems(pageNumber)
//
//    fun getMoreItems(pageNumber: Int) = apply {
//        pageNumberLiveData.value = pageNumber
//    }
}