package com.jayant.proactivist.rest

import com.jayant.proactivist.rest.AddLoggingInterceptor.getInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofitCompany: Retrofit? = null
    private var retrofitNotification: Retrofit? = null
    private var retrofit: Retrofit? = null
    var notificationUrl = "https://fcm.googleapis.com"
    var companySearchBaseUrl = "https://autocomplete.clearbit.com/v1/companies/"
//    var baseUrl = "http://192.168.0.103:8000"
    var baseUrl = "https://limitless-atoll-08063.herokuapp.com"

    var okHttpClient = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .addInterceptor(getInterceptor())
        .build()

    @JvmStatic
    val companySearchClient: Retrofit?
        get() {
            if (retrofitCompany == null) {
                retrofitCompany = Retrofit.Builder()
                    .baseUrl(companySearchBaseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofitCompany
        }

    @JvmStatic
    val client: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }

    @JvmStatic
    val notificationClient: Retrofit?
        get() {
            if (retrofitNotification == null) {
                retrofitNotification = Retrofit.Builder()
                    .baseUrl(notificationUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofitNotification
        }

}