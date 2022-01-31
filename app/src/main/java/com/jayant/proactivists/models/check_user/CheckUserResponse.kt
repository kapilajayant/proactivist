package com.jayant.proactivists.models.check_user

data class CheckUserResponse(
    var user_type: String,
    var is_exist: Boolean
)