package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: String,
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: AuthUser
)