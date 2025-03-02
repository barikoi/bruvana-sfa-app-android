package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class GiftResponse(
    @SerializedName("gifts")
    val gifts: List<Gift>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)

data class Gift(
    @SerializedName("category")
    val category: Category,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class Category(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("updated_at")
    val updatedAt: String
)