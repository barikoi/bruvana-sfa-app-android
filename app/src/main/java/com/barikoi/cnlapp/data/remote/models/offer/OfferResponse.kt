package com.barikoi.cnlapp.data.remote.models.offer


import com.google.gson.annotations.SerializedName

data class OfferResponse(
    @SerializedName("offers")
    val offers: List<Offer>
)


data class Offer(
    @SerializedName("combo_price")
    val comboPrice: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: Boolean?,
    @SerializedName("name")
    val name: String,
    @SerializedName("original_price")
    val originalPrice: String,
    @SerializedName("product_combinations")
    val productCombinations: List<ProductCombination>,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    var quantity: Int = 0
)


data class ProductCombination(
    @SerializedName("product")
    val product: Product,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)

data class Product(
    @SerializedName("discounted_unit_price")
    val discountedUnitPrice: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("unit")
    val unit: String
)