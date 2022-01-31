package com.jayant.proactivist.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class JobItemModel(
    var job_title: String? = "",
    var job_id: String? = "",
    var job_poster: String? = "",
    var experience: String? = "",
    var location: String? = "",
    var summary: String? = "",
    var skills: String? = "",
    var company_name: String? = "",
    var company_logo: String? = "",
    var company_linkedin: String? = ""
) : Parcelable