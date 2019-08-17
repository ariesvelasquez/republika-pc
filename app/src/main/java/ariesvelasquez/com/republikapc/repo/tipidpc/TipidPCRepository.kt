package ariesvelasquez.com.republikapc.repo.tipidpc

import androidx.lifecycle.LiveData
import ariesvelasquez.com.republikapc.AppExecutors
import ariesvelasquez.com.republikapc.api.TipidPcApiService
import ariesvelasquez.com.republikapc.db.TipidPCDao
import ariesvelasquez.com.republikapc.model.SellingItem
import ariesvelasquez.com.republikapc.model.SellingItemResource
import ariesvelasquez.com.republikapc.network.Resource
import ariesvelasquez.com.republikapc.repo.NetworkBoundResource
import ariesvelasquez.com.republikapc.utils.TransformUtil
import timber.log.Timber

class TipidPCRepository(
    private val tipidPcDao: TipidPCDao,
    private val tipidPcService: TipidPcApiService,
    private val executors: AppExecutors = AppExecutors()) {

    fun getTPCSellingItems(page: Int) : LiveData<Resource<List<SellingItem>?>> {

        return object : NetworkBoundResource<List<SellingItem>, List<SellingItemResource>> (executors) {

            override fun saveCallResult(item: List<SellingItemResource>) {
                tipidPcDao.insertSellingItems(TransformUtil().resourceToModel(item))
            }

            override fun shouldFetch(data: List<SellingItem>?): Boolean = true

            override fun loadFromDb() = tipidPcDao.getAllSellingItems()

            override fun createCall(): LiveData<Resource<List<SellingItemResource>>> = tipidPcService.getSellingItems(page = page)

        }.asLiveData()
    }
}