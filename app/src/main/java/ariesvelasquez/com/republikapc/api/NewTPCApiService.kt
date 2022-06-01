package ariesvelasquez.com.republikapc.api

import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import ariesvelasquez.com.republikapc.network.Resource
import ariesvelasquez.com.republikapc.network.Resource2
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NewTPCApiService {

    @GET("tipidpc/user_items/{sellerName}")
    suspend fun getSellerItems(@Path("sellerName") seller: String) : Response<FeedItemsResource>
}