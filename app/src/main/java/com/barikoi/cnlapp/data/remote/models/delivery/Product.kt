package com.barikoi.cnlapp.data.remote.models.delivery


import com.google.gson.annotations.SerializedName

data class Product(
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
    @SerializedName("offer_id")
    val offerId: String,
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