package ariesvelasquez.com.republikapc.api

import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface TipidPCApi {

    @GET("tipidpc/feeds/{page}")
    fun getSellingItems(@Path("page") page: Int) : Call<FeedItemsResource>

    @GET("tipidpc/search/{item}/{page}")
    fun getSearhItem(
        @Path("item") item: String,
        @Path("page") page: Int) : Call<FeedItemsResource>

    @GET("tipidpc/user_items/{sellerName}")
    fun getSellerItems(@Path("sellerName") name: String) : Call<FeedItemsResource>

    @GET("tipidpc/user_search/{sellerName}")
    fun getSellers(@Path("sellerName") name: String) : Call<FeedItemsResource>

    companion object {

        private const val API_BASE_URL = Const.BASE_URL

        // LoggingInterceptor
        private val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        private val client = OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .addInterceptor(loggingInterceptor)
            .build()

        fun create() : TipidPCApi {
            return Retrofit.Builder()
                .baseUrl(HttpUrl.parse(API_BASE_URL)!!)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TipidPCApi::class.java)
        }
    }
}