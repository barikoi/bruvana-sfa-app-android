package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class ConfirmOrderRequest(
    @SerializedName("orders")
    val orders: List<Order>
)

data class Order(
    @SerializedName("distance_from_outlets")
    val distanceFromOutlets: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("order_end_time")
    val orderEndTime: String,
    @SerializedName("order_start_time")
    val orderStartTime: String,
    @SerializedName("ordered_at")
    val orderedAt: String,
    @SerializedName("outlet_id")
    val outletId: String,
    @SerializedName("products")
    val products: List<ProductRe>,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: String,
    @SerializedName("user_id")
    val userId: String
)

data class ProductRe(
    @SerializedName("bounced_amount")
    val bouncedAmount: String,
    @SerializedName("bounced_quantity")
    val bouncedQuantity: String,
    @SerializedName("category_code")
    val categoryCode: String,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("delivered_amount")
    val deliveredAmount: String,
    @SerializedName("delivered_quantity")
    val deliveredQuantity: String,
    @SerializedName("discounted_unit_price")
    val discountedUnitPrice: String,
    @SerializedName("ordered_amount")
    val orderedAmount: String,
    @SerializedName("ordered_quantity")
    val orderedQuantity: String,
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("sku_code")
    val skuCode: String,
    @SerializedName("unit_code")
    val unitCode: String,
    @SerializedName("unit_id")
    val unitId: String,
    @SerializedName("unit_name")
    val unitName: String,
    @SerializedName("unit_price")
    val unitPrice: String
)