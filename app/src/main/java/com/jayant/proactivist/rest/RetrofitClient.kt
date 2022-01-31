package com.jayant.proactivist.rest

import com.jayant.proactivist.rest.AddLoggingInterceptor.getInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofitCompany: Retrofit? = null
    private var retrofit: Retrofit? = null
    var companySearchBaseUrl = "https://autocomplete.clearbit.com/v1/companies/"
    var baseUrl = "http://192.168.0.102:8000"
//    var baseUrl = "https://staging.proactivist.in"
//    var baseUrl = "https://prod.proactivist.in"

    var okHttpClient = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
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
}