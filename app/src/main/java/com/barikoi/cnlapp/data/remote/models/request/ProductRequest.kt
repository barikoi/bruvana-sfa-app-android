package com.barikoi.cnlapp.data.remote.models.request

import com.google.gson.annotations.SerializedName

data class OrderRequest(
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
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("products")
    val products: List<ProductRequest>,

    )


data class ProductRequest(

    @SerializedName("product_id") val productId: String,
    @SerializedName("product_code") val productCode: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("sku_code") val skuCode: String,
    @SerializedName("unit_id") val unitId: String,
    @SerializedName("unit_name") val unitName: String,
    @SerializedName("unit_code") val unitCode: String,
    @SerializedName("unit_price") val unitPrice: String,
    @SerializedName("discounted_unit_price") val discountedUnitPrice: String,
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("category_code") val categoryCode: String,
    @SerializedName("ordered_quantity") val orderedQuantity: String,
    @SerializedName("delivered_quantity") val deliveredQuantity: String = "0",
    @SerializedName("bounced_quantity") val bouncedQuantity: String = "0",
    @SerializedName("ordered_amount") val orderedAmount: String,
    @SerializedName("delivered_amount") val deliveredAmount: String = "0",
    @SerializedName("bounced_amount") val bouncedAmount: String = "0"
)