package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class ApproveRequest(
    @SerializedName("request_id")
    val requestId: Int,
    @SerializedName("status")
    val status: String
)