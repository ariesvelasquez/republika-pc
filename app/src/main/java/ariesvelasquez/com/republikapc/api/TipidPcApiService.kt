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

//    @GET("tipidpc/sell/{page}")
//    fun getSellingItems(@Path("page") page: Int) : LiveData<Resource<List<FeedItemsResource>>>

    @GET("tipidpc/feeds/{page}")
    fun getSellingItems(@Path("page") page: Int) : Call<FeedItemsResource>

//    class ListingResponse(val data: ListingData)
//
//    class ListingData(
//        val children: List<TipidPCChildrenResponse>
//    )
//
//    data class TipidPCChildrenResponse(val data: FeedItem)

    companion object {

        private const val API_BASE_URL = Const.BASE_URL

        // LoggingInterceptor
        private val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        var authorization = Interceptor{ chain ->
            val newRequest = chain.request().newBuilder()
//                .addHeader("Authorization", _base64)
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(newRequest)
        }

        private val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        fun create() : TipidPCApi {
            return Retrofit.Builder()
                .baseUrl(HttpUrl.parse(API_BASE_URL)!!)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(TipidPCApi::class.java)
        }
    }
}