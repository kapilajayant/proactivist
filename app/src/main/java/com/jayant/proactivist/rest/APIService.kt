package com.jayant.proactivist.rest;

import com.jayant.proactivist.models.ListResponseModel
import com.jayant.proactivist.models.ResponseModel
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface APIService {

    @GET("/rest/user/exists/{uid}")
    fun checkUser(@Path("uid") uid: String): Call<ResponseModel>

    @GET("/rest/profile/get/{uid}")
    fun getProfile(@Path("uid") uid: String, @Query("user_type") user_type: String): Call<ResponseModel>

    @GET("/rest/referrers/get/{uid}")
    fun getReferrers(@Path("uid") uid: String): Call<ListResponseModel>

    @GET("/rest/candidates/get/{uid}")
    fun getCandidates(@Path("uid") uid: String): Call<ListResponseModel>

    @GET("/rest/applications/get/{uid}")
    fun getApplications(@Path("uid") uid: String, @Query("user_type") user_type: String, @Query("status") status: String): Call<ListResponseModel>

    @POST("/rest/profile/create")
    fun createProfile(@Body body: RequestBody): Call<ResponseModel>

    @PUT("/rest/application/create")
    fun createApplication(@Body body: RequestBody): Call<ResponseModel>

    @PUT("/rest/status/update")
    fun updateApplication(@Body body: RequestBody): Call<ResponseModel>

    @PUT("/rest/profile/update/{uid}")
    fun updateProfile(@Path("uid") uid: String, @Query("user_type") user_type: String, @Body body: RequestBody): Call<ResponseModel>

    @GET("/rest/coins/get")
    fun getCoins(@Query("uid") uid: String): Call<ResponseModel>

    @GET("/rest/application/get")
    fun getApplicationById(@Query("uid") uid: String, @Query("application_id") application_id: String): Call<ResponseModel>

    @PUT("/rest/invite/add/")
    fun addInviteCode(@Query("invite_code") invite_code: String, @Query("uid") uid: String): Call<ResponseModel>

    @GET("/rest/search")
    fun search(@Query("search_term") invite_code: String): Call<ListResponseModel>

    @GET("/rest/feedback/post")
    fun feedback(@Body body: RequestBody): Call<ResponseModel>

}