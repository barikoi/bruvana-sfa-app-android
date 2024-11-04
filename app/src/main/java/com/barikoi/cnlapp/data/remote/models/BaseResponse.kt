package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)