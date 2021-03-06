package com.jayant.proactivist.models

import android.os.Parcelable
import com.jayant.proactivist.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Application(
    var uid: String? = "",
    var job_id: String? = "",
    var application_id: String? = "",
    var name: String? = "",
    var photo: String? = "",
    var company_name: String? = "",
    var company_logo: String? = "",
    var status: Int? = Constants.PENDING,
    var timestamp: String? = "",
    var feedback: String? = "",
) : Parcelable