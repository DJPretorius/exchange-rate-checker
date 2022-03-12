package com.example.exchangerates.modles.api

import android.content.Context
import com.example.exchangerates.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface RetrofitAPI {

    @GET("/apilive")
    suspend fun getExchangeRate(
        @QueryMap params : Map<String, String>
    ) : Response<ResponseBody>

    @GET("/apitimeseries")
    suspend fun getTimeSeries(
        @QueryMap params : Map<String, String>
    ) : Response<ResponseBody>

    @GET("/apicurrencies")
    suspend fun getCurrencies(
        @Query("api_key") apiKey : String
    ) : Response<ResponseBody>

    companion object {
        var instance : RetrofitAPI? = null

        fun create(context : Context): RetrofitAPI {
            if (instance == null) {
                val builder = Retrofit.Builder()
                    .baseUrl("https://fxmarketapi.com")

                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.level = HttpLoggingInterceptor.Level.BODY
                    val client = OkHttpClient.Builder().addInterceptor(logging).build()
                    builder.client(client)
                }

                val retrofit = builder.build()

                instance = retrofit.create(RetrofitAPI::class.java)
            }

            return instance!!
        }
    }
}