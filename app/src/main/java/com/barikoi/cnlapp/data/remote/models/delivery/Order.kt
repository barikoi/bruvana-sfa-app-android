package com.barikoi.cnlapp.data.remote.models.delivery


import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("area_code")
    val areaCode: String,
    @SerializedName("area_name")
    val areaName: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_address")
    val dbHouseAddress: String,
    @SerializedName("db_house_code")
    val dbHouseCode: String,
    @SerializedName("db_house_latitude")
    val dbHouseLatitude: Any,
    @SerializedName("db_house_longitude")
    val dbHouseLongitude: Any,
    @SerializedName("db_house_name")
    val dbHouseName: String,
    @SerializedName("db_house_owner_name")
    val dbHouseOwnerName: String,
    @SerializedName("db_house_owner_phone_number")
    val dbHouseOwnerPhoneNumber: String,
    @SerializedName("delivered_at")
    val deliveredAt: Any,
    @SerializedName("distance_from_outlets")
    val distanceFromOutlets: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: String,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("offer_applied")
    val offerApplied: Int,
    @SerializedName("order_end_time")
    val orderEndTime: String,
    @SerializedName("order_id")
    val orderId: String?,
    @SerializedName("order_no")
    val orderNo: String,
    @SerializedName("order_start_time")
    val orderStartTime: String,
    @SerializedName("order_status")
    val orderStatus: String,
    @SerializedName("order_tracking_time")
    val orderTrackingTime: String,
    @SerializedName("ordered_at")
    val orderedAt: String,
    @SerializedName("outlet_address")
    val outletAddress: String,
    @SerializedName("outlet_category")
    val outletCategory: Any,
    @SerializedName("outlet_id")
    val outletId: Int,
    @SerializedName("outlet_latitude")
    val outletLatitude: String,
    @SerializedName("outlet_longitude")
    val outletLongitude: String,
    @SerializedName("outlet_name")
    val outletName: String,
    @SerializedName("outlet_type")
    val outletType: String,
    @SerializedName("owner_name")
    val ownerName: Any,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("phone_number")
    val phoneNumber: Any,
    @SerializedName("products")
    val products: List<Product>,
    @SerializedName("region_code")
    val regionCode: String,
    @SerializedName("region_name")
    val regionName: String,
    @SerializedName("route_code")
    val routeCode: String,
    @SerializedName("route_id")
    val routeId: Int,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("shop_id")
    val shopId: Any,
    @SerializedName("territory_code")
    val territoryCode: Any,
    @SerializedName("territory_name")
    val territoryName: Any,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: Any,
    @SerializedName("total_bounced_quantity")
    val totalBouncedQuantity: Any,
    @SerializedName("total_delivered_amount")
    val totalDeliveredAmount: Any,
    @SerializedName("total_delivered_quantity")
    val totalDeliveredQuantity: Any,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String,
    @SerializedName("total_ordered_quantity")
    val totalOrderedQuantity: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
)