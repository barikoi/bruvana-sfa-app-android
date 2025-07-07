package com.barikoi.cnlapp.data.remote.models.product


import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("product_count")
    val productCount: Int,
    @SerializedName("products")
    val products: List<Product>,
    @SerializedName("status_code")
    val statusCode: Int
)