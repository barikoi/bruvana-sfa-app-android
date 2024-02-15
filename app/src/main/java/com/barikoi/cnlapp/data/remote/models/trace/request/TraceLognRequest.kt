package com.barikoi.cnlapp.data.remote.models.trace.request


import com.google.gson.annotations.SerializedName

data class TraceLognRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)