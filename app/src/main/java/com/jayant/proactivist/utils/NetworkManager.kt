package com.jayant.proactivist.utils

import android.content.Context
import android.net.ConnectivityManager
import com.jayant.proactivist.fragments.NoInternetFragment

object NetworkManager {
    fun getConnectivityStatusString(context: Context): Int? {
        var status: Int? = null
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                status = Constants.WIFI
                return status
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                status = Constants.MOBILE_DATA
                return status
            }
        } else {
            status = Constants.NO_INTERNET
            return status
        }
        return status
    }
}