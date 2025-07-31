package com.barikoi.cnlapp.data.remote.models.product


import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("brand_id")
    val brandId: Int,
    @SerializedName("category_code")
    val categoryCode: String,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("current_available_stock")
    val currentAvailableStock: Int,
    @SerializedName("current_stock")
    val currentStock: Int,
    @SerializedName("discount_amount")
    val discountAmount: String?,
    @SerializedName("discounted_unit_price")
    val discountedUnitPrice: Double,
    @SerializedName("dp_price")
    val dpPrice: String,
    @SerializedName("dp_price_cartoon")
    val dpPriceCartoon: String?,
    @SerializedName("etp_price_cartoon")
    val etpPriceCartoon: String?,
    @SerializedName("etp_price_pack_jar")
    val etpPricePackJar: String?,
    @SerializedName("etp_price_unit")
    val etpPriceUnit: String?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: List<Image>,
    @SerializedName("initial_available_stock")
    val initialAvailableStock: Int,
    @SerializedName("quantity_last_month")
    val quantityLastMonth: Int,
    @SerializedName("initial_stock")
    val initialStock: Int,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("model_no")
    val modelNo: String?,
    @SerializedName("pro_code")
    val proCode: String,
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("sku_code")
    val skuCode: String,
    @SerializedName("tp_price_cartoon")
    val tpPriceCartoon: String,
    @SerializedName("unit_code")
    val unitCode: String,
    @SerializedName("unit_id")
    val unitId: Int,
    @SerializedName("unit_name")
    val unitName: String,
    @SerializedName("unit_price")
    val unitPrice: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    var qty: Int = 0,
    var offerId: String? = ""
)