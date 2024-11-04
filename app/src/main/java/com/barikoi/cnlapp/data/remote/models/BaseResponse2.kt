package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class BaseResponse2(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
)