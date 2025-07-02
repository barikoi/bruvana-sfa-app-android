package com.barikoi.cnlapp.data.remote.models.pre_order


import com.google.gson.annotations.SerializedName

data class PreviousDayOrderResponse(
    @SerializedName("outlets")
    val outlets: List<Outlet>,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("total_outlet_count")
    val totalOutletCount: Int
)


data class Order(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("delivered_at")
    val deliveredAt: String?,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("order_no")
    val orderNo: String,
    @SerializedName("order_status")
    val orderStatus: String,
    @SerializedName("ordered_at")
    val orderedAt: String,
    @SerializedName("outlet_id")
    val outletId: Int,
    @SerializedName("products")
    val products: List<Product>,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: String,
    @SerializedName("total_bounced_quantity")
    val totalBouncedQuantity: Int,
    @SerializedName("total_delivered_amount")
    val totalDeliveredAmount: String,
    @SerializedName("total_delivered_quantity")
    val totalDeliveredQuantity: Int,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int
)


data class Outlet(
    @SerializedName("address")
    val address: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by_employee_id")
    val createdByEmployeeId: String,
    @SerializedName("created_by_user_id")
    val createdByUserId: Int,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_buyer")
    val isBuyer: Int,
    @SerializedName("is_edited")
    val isEdited: Int,
    @SerializedName("is_verified")
    val isVerified: Int,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("market_opportunity")
    val marketOpportunity: String?,
    @SerializedName("minimum_order")
    val minimumOrder: String?,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("orders")
    val orders: List<Order>,
    @SerializedName("outlet_category")
    val outletCategory: String?,
    @SerializedName("outlet_code")
    val outletCode: String?,
    @SerializedName("outlet_created_at")
    val outletCreatedAt: String,
    @SerializedName("outlet_name")
    val outletName: String,
    @SerializedName("outlet_status")
    val outletStatus: String,
    @SerializedName("outlet_type")
    val outletType: String,
    @SerializedName("owner_name")
    val ownerName: String,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("route_id")
    val routeId: Int,
    @SerializedName("shop_id")
    val shopId: String?,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: String,
    @SerializedName("total_bounced_quantity")
    val totalBouncedQuantity: Int,
    @SerializedName("total_delivered_amount")
    val totalDeliveredAmount: Double,
    @SerializedName("total_delivered_quantity")
    val totalDeliveredQuantity: Int,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: Double,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("updated_by_user_id")
    val updatedByUserId: String?
)


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