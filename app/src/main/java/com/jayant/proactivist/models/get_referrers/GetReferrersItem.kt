package com.jayant.proactivist.models.get_referrers

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetReferrersItem(
    var ref_gid: String,
    var job_id: String? = "",
    var referrer_name: String,
    var company_logo: String,
    var company_name: String,
    var company_linkedin: String,
    var token: String,
):Parcelable