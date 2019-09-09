package ariesvelasquez.com.republikapc.repository.tipidpc

@Deprecated("For Now")
class TipidPCRepositoryNBR() {
//    private val tipidPcDao: TipidPCDao,
//    private val tipidPcService: TipidPCApi,
//    private val executors: AppExecutors = AppExecutors()) {
//
//    fun getTPCSellingItems(page: Int) : LiveData<Resource<List<FeedItem>?>> {
//
//        return object : NetworkBoundResource<List<FeedItem>, List<FeedItemsResource>> (executors) {
//
//            override fun saveCallResult(item: List<FeedItemsResource>) {
//                tipidPcDao.insertSellingItems(TransformUtil().resourceToModel(item))
//            }
//
//            override fun shouldFetch(data: List<FeedItem>?): Boolean = true
//
//            override fun loadFromDb() = tipidPcDao.getAllSellingItems()
//
//            override fun createCall(): LiveData<Resource<List<FeedItemsResource>>> = tipidPcService.getSellingItems(page = page)
//
//        }.asLiveData()
//    }
}