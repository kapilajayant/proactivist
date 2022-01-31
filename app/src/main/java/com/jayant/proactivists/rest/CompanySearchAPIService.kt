package com.jayant.proactivists.rest;

import com.jayant.proactivists.models.CompanySuggestion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CompanySearchAPIService {

    @GET("suggest")
    fun search(@Query("query") query: String): Call<ArrayList<CompanySuggestion>>
}