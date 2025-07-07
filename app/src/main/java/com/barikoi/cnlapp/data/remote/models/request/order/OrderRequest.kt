package com.barikoi.cnlapp.data.remote.models.request.order


import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("outlet_id")
    val outletId: String,
    @SerializedName("distance_from_outlets")
    val distanceFromOutlets: String,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("offer_applied")
    val offerApplied: String,
    @SerializedName("order_start_time")
    val orderStartTime: String,
    @SerializedName("order_end_time")
    val orderEndTime: String,
    @SerializedName("ordered_at")
    val orderedAt: String,
    @SerializedName("products")
    val products: List<ProductRequest>?,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: String?,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String?
)