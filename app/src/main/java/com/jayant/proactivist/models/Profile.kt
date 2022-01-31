package com.jayant.proactivist.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Profile(
    var uid: String? = "",
    var name: String? = "",
    var phone: String? = "",
    var photo: String? = "",
    var email: String? = "",
    var experience: String? = "",
    var position: String? = "",
    var skills: String? = "",
    var personal_linkedin: String? = "",
    var role: String? = "",
    var about: String? = "",
    var resume: String? = "",
    var comp_id: String? = "",
    var company_name: String? = "",
    var company_logo: String? = "",
    var company_linkedin: String? = "",
) : Parcelable