package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class GIftSummaryResponse(
    @SerializedName("gifts")
    val gifts: List<GiftSummary>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)

data class GiftSummary(
    @SerializedName("data")
    val giftData: List<GiftData>,
    @SerializedName("Name")
    val name: String,
    @SerializedName("total")
    val total: Int,
    var isExpanded: Boolean = false
)


data class GiftData(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: Int
)