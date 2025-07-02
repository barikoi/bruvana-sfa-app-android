package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class CheckAttendanceResponse(
    @SerializedName("attendances")
    val attendances: List<Attendance>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)

data class Attendance(
    @SerializedName("checkin_address")
    val checkinAddress: String?,
    @SerializedName("checkin_time")
    val checkinTime: String?,
    @SerializedName("checkout_address")
    val checkoutAddress: String?,
    @SerializedName("checkout_images")
    val checkoutImages: String?,
    @SerializedName("checkout_lat")
    val checkoutLat: String?,
    @SerializedName("checkout_lon")
    val checkoutLon: String?,
    @SerializedName("checkout_time")
    val checkoutTime: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: List<Image>?,
    @SerializedName("is_absent")
    val isAbsent: Int,
    @SerializedName("is_late")
    val isLate: Int,
    @SerializedName("latitude")
    val latitude: String?,
    @SerializedName("longitude")
    val longitude: String?,
    @SerializedName("remarks")
    val remarks: String?,
    @SerializedName("route_id")
    val routeId: String?,
    @SerializedName("route_name")
    val routeName: String?,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
)