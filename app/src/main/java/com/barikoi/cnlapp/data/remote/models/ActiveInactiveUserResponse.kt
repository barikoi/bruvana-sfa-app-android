package com.barikoi.cnlapp.data.remote.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ActiveInactiveUserResponse(
    @SerializedName("active")
    val active: List<ActiveInactiveUser>,
    @SerializedName("inactive")
    val inactive: List<ActiveInactiveUser>,
    @SerializedName("status_code")
    val statusCode: Int
)


@Parcelize
data class ActiveInactiveUser(
    @SerializedName("checkin_address")
    val checkinAddress: String?,
    @SerializedName("checkin_time")
    val checkinTime: String?,
    @SerializedName("checkout_time")
    val checkoutTime: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image")
    val image: List<Image>?,
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
    val routeId: Int?,
    @SerializedName("route_name")
    val routeName: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String
) : Parcelable