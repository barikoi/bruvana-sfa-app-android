package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class OrderResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("orders")
    val orders: List<OrderData>,
    @SerializedName("status_code")
    val statusCode: Int
)

data class OrderData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("order_no")
    val orderNo: String,
    @SerializedName("order_status")
    val orderStatus: String
)