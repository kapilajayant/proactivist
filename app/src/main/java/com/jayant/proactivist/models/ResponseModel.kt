package com.jayant.proactivist.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.jayant.proactivist.models.check_user.CheckUserResponse
import com.jayant.proactivist.models.get_candidates.GetCandidatesItem
import com.jayant.proactivist.models.get_referrers.GetReferrersItem

data class ResponseModel(
    var status: String,
    var message: String,
    var data: JsonObject
) {
    fun getProfileResponse(): Profile {
        val gson = Gson()
        val itemType = object : TypeToken<Profile>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

    fun getCheckUserResponse(): CheckUserResponse {
        val gson = Gson()
        val itemType = object : TypeToken<CheckUserResponse>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

    fun getCoinsResponse(): Coins {
        val gson = Gson()
        val itemType = object : TypeToken<Coins>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

    fun getApplicationStatusResponse(): ApplicationStatus {
        val gson = Gson()
        val itemType = object : TypeToken<ApplicationStatus>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

}
data class ListResponseModel(
    var status: String,
    var message: String,
    var data: JsonArray
) {

    fun getReferrersResponse(): ArrayList<GetReferrersItem> {
        val gson = Gson()
        val itemType = object : TypeToken<ArrayList<GetReferrersItem>>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

    fun getCandidatesResponse(): ArrayList<GetCandidatesItem> {
        val gson = Gson()
        val itemType = object : TypeToken<ArrayList<GetCandidatesItem>>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }

    fun getApplicationsResponse(): ArrayList<Application> {
        val gson = Gson()
        val itemType = object : TypeToken<ArrayList<Application>>() {}.type
        return gson.fromJson(this.data.toString(), itemType)
    }
}

data class ApplicationStatus(
    var status: String
)
data class Coins(
    var coins: Int,
    var invite_code: String,
    var already_invited: Boolean
)