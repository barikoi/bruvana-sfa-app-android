package com.barikoi.cnlapp.data.remote.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class OutletsResponse(
    @SerializedName("outlets")
    val outlets: List<Outlet>,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("total_outlet_count")
    val totalOutletCount: Int,
    @SerializedName("total_updated_outlet_count")
    val totalUpdatedOutletCount: Int
)


@Parcelize
data class Outlet(
    @SerializedName("address")
    val address: String,
    @SerializedName("area_code")
    val areaCode: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("area_name")
    val areaName: String,
    @SerializedName("db_house_name")
    val dbHouseName: String?,
    @SerializedName("db_house_owner_name")
    val dbHouseOwnerName: String?,
    @SerializedName("db_house_owner_phone_number")
    val dbHouseOwnerPhoneNumber: String?,
    @SerializedName("employee_id")
    val employeeId: String?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: List<Image>? = emptyList(),
    @SerializedName("is_buyer")
    val isBuyer: Int?,
    @SerializedName("is_edited")
    val isEdited: Int?,
    @SerializedName("is_verified")
    val isVerified: Int,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("market_opportunity")
    val marketOpportunity: String?,
    @SerializedName("minimum_order")
    val minimumOrder: Int,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("outlet_category")
    val outletCategory: String,
    @SerializedName("outlet_code")
    val outletCode: String?,
    @SerializedName("outlet_created_at")
    val outletCreatedAt: String?,
    @SerializedName("outlet_name")
    val outletName: String,
    @SerializedName("outlet_status")
    val outletStatus: String,
    @SerializedName("outlet_type")
    val outletType: String,
    @SerializedName("outlet_updated_at")
    val outletUpdatedAt: String?,
    @SerializedName("outlet_verified_at")
    val outletVerifiedAt: String?,
    @SerializedName("owner_name")
    val ownerName: String,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("ordered_today")
    val orderedToday: Int,
    @SerializedName("is_no_order")
    val isNoOrder: Int,
    @SerializedName("phone_number")
    val phoneNumber: String?,
    @SerializedName("region_code")
    val regionCode: String?,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("region_name")
    val regionName: String?,
    @SerializedName("route_code")
    val routeCode: String?,
    @SerializedName("route_id")
    val routeId: Int?,
    @SerializedName("route_name")
    val routeName: String?,
    @SerializedName("shop_id")
    val shopId: String?,
    @SerializedName("territory_code")
    val territoryCode: String?,
    @SerializedName("territory_id")
    val territoryId: Int?,
    @SerializedName("territory_name")
    val territoryName: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("updated_by_employee_id")
    val updatedByEmployeeId: String?,
    @SerializedName("updated_by_user_id")
    val updatedByUserId: Int?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_name")
    val userName: String?,
    @SerializedName("verified_by_employee_id")
    val verifiedByEmployeeId: String?,
    @SerializedName("verified_by_user_id")
    val verifiedByUserId: String?,
    @SerializedName("last_ordered_at")
    val lastOrderedAt: String?,
    val distance: Float = 0f,
): Parcelable

@Parcelize
data class Image(
    @SerializedName("image_url")
    val imageUrl: String
): Parcelable