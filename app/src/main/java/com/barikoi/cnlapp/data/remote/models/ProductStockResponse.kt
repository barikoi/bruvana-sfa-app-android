package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class ProductStockResponse(
    @SerializedName("product_count")
    val productCount: Int,
    @SerializedName("products")
    val products: List<Product>,
    @SerializedName("status_code")
    val statusCode: Int
)

data class Product(
    @SerializedName("bounced_amount")
    val bouncedAmount: Int,
    @SerializedName("bounced_quantity")
    val bouncedQuantity: Int,
    @SerializedName("category_code")
    val categoryCode: String,
    @SerializedName("category_id")
    val categoryId: Int,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("current_available_sto ck")
    val currentAvailableStoCk: Int,
    @SerializedName("current_available_stock")
    val currentAvailableStock: Int,
    @SerializedName("current_stock")
    val currentStock: Int,
    @SerializedName("del ivered_amount")
    val delIveredAmount: Int,
    @SerializedName("delivered_amount")
    val deliveredAmount: Double,
    @SerializedName("delivered_quantity")
    val deliveredQuantity: Int,
    @SerializedName("discount_amount")
    val discountAmount: String?,
    @SerializedName("discounted_unit_price")
    val discountedUnitPrice: Double,
    @SerializedName("dp_price")
    val dpPrice: String?,
    @SerializedName("dp_price_cartoon")
    val dpPriceCartoon: String?,
    @SerializedName("etp_price_carton")
    val etpPriceCarton: String?,
    @SerializedName("etp_price_pack_jar")
    val etpPricePackJar: String?,
    @SerializedName("etp_price_unit")
    val etpPriceUnit: String?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: List<Image>,
    @SerializedName("initi al_available_stock")
    val initiAlAvailableStock: Int,
    @SerializedName("initial_avai lable_stock")
    val initialAvaiLableStock: Int,
    @SerializedName("initial_available_stock")
    val initialAvailableStock: Int,
    @SerializedName("initial_stock")
    val initialStock: Int,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("ordered_amount")
    val orderedAmount: Double,
    @SerializedName("ordered_quantity")
    val orderedQuantity: Int,
    @SerializedName("pro_code")
    val proCode: String?,
    @SerializedName("pro ductive_routes")
    val proDuctiveRoutes: Int,
    @SerializedName("product_code")
    val productCode: String,
    @SerializedName("product ive_outlets")
    val productIveOutlets: Int,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("producti ve_outlets")
    val productiVeOutlets: Int,
    @SerializedName("productive_outlets")
    val productiveOutlets: Int,
    @SerializedName("productive_route s")
    val productiveRouteS: Int,
    @SerializedName("productive_routes")
    val productiveRoutes: Int,
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


    var isSelect: Boolean = false,
    var stockValue: String = "0"
)