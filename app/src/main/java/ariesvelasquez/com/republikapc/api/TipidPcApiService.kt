package ariesvelasquez.com.republikapc.api

import androidx.lifecycle.LiveData
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.model.SellingItemResource
import ariesvelasquez.com.republikapc.network.Resource
import ariesvelasquez.com.republikapc.utils.LiveDataCallAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber
import java.util.*

interface TipidPcApiService {

    @GET("tipidpc/sell/{page}")
    fun getSellingItems(@Path("page") page: Int) : LiveData<Resource<List<SellingItemResource>>>

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

        fun getTPCApiService() : TipidPcApiService {
            return Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(TipidPcApiService::class.java)
        }
    }
}