package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class TraceLoginResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("token")
    val token: String,
)