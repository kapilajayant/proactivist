package com.jayant.proactivist.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CompanyItem(
    var company_name: String,
    var company_logo: String,
    var company_linkedin: String,
) : Parcelable