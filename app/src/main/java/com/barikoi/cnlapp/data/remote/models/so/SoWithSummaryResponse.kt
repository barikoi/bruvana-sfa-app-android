package com.barikoi.cnlapp.data.remote.models.so


import com.barikoi.cnlapp.data.remote.models.So
import com.google.gson.annotations.SerializedName

data class SoWithSummaryResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("so_list")
    val soList: List<So>,
    @SerializedName("status_code")
    val statusCode: Int
)