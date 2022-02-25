package com.jayant.proactivist.utils

import com.google.gson.JsonObject
import com.jayant.proactivist.rest.APIService
import com.jayant.proactivist.rest.ApiUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object NotificationManager {

    private lateinit var notificationApiService: APIService

    fun sendNotification(token: String, type: Int, can: String, ref: String) {

        notificationApiService = ApiUtils.getNotificationAPIService()

        var title = ""
        var message = ""
        var url = ""

        when(type){
            Constants.ACCEPTED ->{
                url = "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/Work%20risk-free.png?alt=media&token=4b00e39c-8c7a-4068-a921-b29e8e8a9aae"
                title = "Application Accepted!"
                message = "Your application has been Accepted by the referrer. Tap to know more."
            }
            Constants.REJECTED ->{
                title = "Application Rejected!"
                message = "Your application has been Rejected by the referrer. Tap to know more."
            }
            Constants.COMPLETED ->{
                url = "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/Work%20risk-free.png?alt=media&token=4b00e39c-8c7a-4068-a921-b29e8e8a9aae"
                title = "Application Complete!"
                message = "Your application process is complete. Tap to know more."
            }
            Constants.PENDING ->{
                url = "https://firebasestorage.googleapis.com/v0/b/proactivists-ba8ef.appspot.com/o/Analyse%20a%20candidate%26%2339%3Bs%20profile.png?alt=media&token=f7f3c8c4-b828-48a3-981f-7028cf0a1270"
                title = "New application request!"
                message = "You have a new application. Tap to know more."
            }
        }

        val token = token
        val jsonObject = JSONObject()
        jsonObject.put("to",token)

        val dataObject = JSONObject()
        dataObject.put("success", true)
        dataObject.put("type", type)
        dataObject.put("url", url)
        dataObject.put("title", title)
        dataObject.put("message", message)
        val innerData = JSONObject()
        innerData.put("can", can)
        innerData.put("ref", ref)
        dataObject.put("data", innerData)
        jsonObject.put("data", dataObject)

        val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString())
        notificationApiService.sendNotification(body).enqueue(object : Callback<JsonObject> {
            override fun onResponse(
                call: Call<JsonObject>,
                response: Response<JsonObject>
            ) {
                try {

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

}