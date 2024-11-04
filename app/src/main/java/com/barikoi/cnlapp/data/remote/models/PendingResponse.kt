package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class PendingResponse(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)

data class Data(
    @SerializedName("notViewed")
    val notViewed: Int,
    @SerializedName("pendingRequests")
    val pendingRequests: Int
)