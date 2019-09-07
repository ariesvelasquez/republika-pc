package ariesvelasquez.com.republikapc.api

import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.model.feeds.FeedItemsResource
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface TipidPCApi {

    @GET("tipidpc/feeds/{page}")
    fun getSellingItems(@Path("page") page: Int) : Call<FeedItemsResource>

    @GET("tipidpc/search/{item}/[page]")
    fun searchItem(
        @Path("item") item: String,
        @Path("page") page: Int) : Call<FeedItemsResource>

    @GET("tipidpc/seller/{name}/{page}")
    fun searchSellet(
        @Path("name") name: String,
        @Path("page") page: Int
    ) : Call<FeedItemsResource>

    companion object {

        private const val API_BASE_URL = Const.BASE_URL

        // LoggingInterceptor
        private val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        private val client = OkHttpClient.Builder()
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