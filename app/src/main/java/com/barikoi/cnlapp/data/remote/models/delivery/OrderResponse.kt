package com.barikoi.cnlapp.data.remote.models.delivery


import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("orders")
    val orders: List<Order>,
    @SerializedName("status_code")
    val statusCode: Int
)