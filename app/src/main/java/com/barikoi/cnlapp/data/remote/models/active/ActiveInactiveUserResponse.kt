package com.barikoi.cnlapp.data.remote.models.active


import com.google.gson.annotations.SerializedName

data class ActiveInactiveUserResponse(
    @SerializedName("active")
    val active: List<Active>,
    @SerializedName("inactive")
    val inactive: List<Inactive>,
    @SerializedName("status_code")
    val statusCode: Int
)


data class Inactive(
    @SerializedName("checkin_address")
    val checkinAddress: Any,
    @SerializedName("checkin_time")
    val checkinTime: Any,
    @SerializedName("checkout_time")
    val checkoutTime: Any,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: List<Any>,
    @SerializedName("is_absent")
    val isAbsent: Int,
    @SerializedName("is_late")
    val isLate: Int,
    @SerializedName("latitude")
    val latitude: Any,
    @SerializedName("longitude")
    val longitude: Any,
    @SerializedName("remarks")
    val remarks: Any,
    @SerializedName("route_id")
    val routeId: Any,
    @SerializedName("route_name")
    val routeName: Any,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
)


data class Active(
    @SerializedName("checkin_address")
    val checkinAddress: String,
    @SerializedName("checkin_time")
    val checkinTime: String,
    @SerializedName("checkout_time")
    val checkoutTime: Any,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: List<Image>,
    @SerializedName("is_absent")
    val isAbsent: Int,
    @SerializedName("is_late")
    val isLate: Int,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("remarks")
    val remarks: Any,
    @SerializedName("route_id")
    val routeId: Int,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
)


data class Image(
    @SerializedName("image_url")
    val imageUrl: String
)