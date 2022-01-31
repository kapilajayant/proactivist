package com.jayant.proactivist.models.get_candidates

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetCandidatesItem(
    var job_id: String?,
    var application_id: String? = "",
    var can_gid: String,
    var can_name: String,
    var can_photo: String,
    var status: Int,
    var timestamp: String,
    var company_linkedin: String,
    var company_logo: String,
    var company_name: String,
): Parcelable