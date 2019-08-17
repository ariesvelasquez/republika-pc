package ariesvelasquez.com.republikapc.screens.tipidpc

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ariesvelasquez.com.republikapc.api.TipidPcApiService
import ariesvelasquez.com.republikapc.db.TipidPCDatabase
import ariesvelasquez.com.republikapc.model.SellingItem
import ariesvelasquez.com.republikapc.network.Resource
import ariesvelasquez.com.republikapc.repo.tipidpc.TipidPCRepository

class TipidPcViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * List of selling items shown in the ui
     */
    var sellingItemsLiveData = MutableLiveData<Resource<MutableList<SellingItem>?>>()

    // Define the repository
    private var tipidPcRepository: TipidPCRepository = TipidPCRepository(
        TipidPCDatabase.getDatabase(application).tipidPCDao(),
        TipidPcApiService.getTPCApiService()
    )

    fun getSellingItem2(pageNumber: Int) : LiveData<Resource<List<SellingItem>?>> = tipidPcRepository.getTPCSellingItems(pageNumber)

    /**
     * Get more items
     */
    fun getSellingItems(pageNumber: Int) {
        tipidPcRepository.getTPCSellingItems(pageNumber).run {
            sellingItemsLiveData.value?.data?.addAll(this.value?.data!!)
        }
//        val newItems =
//        sellingItemsLiveData.value?.data?.addAll(newItems.value?.data!!)
    }

}