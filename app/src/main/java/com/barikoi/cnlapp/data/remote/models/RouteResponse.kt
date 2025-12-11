package com.barikoi.cnlapp.data.remote.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class RouteResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("routes")
    val routes: List<Route>,
    @SerializedName("status_code")
    val statusCode: Int
)

@Parcelize
data class Route(
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("area_name")
    val areaName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("route_code")
    val routeCode: String,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("outlet_count")
    val outletCount: String?,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("territory_name")
    val territoryName: String,
    @SerializedName("user_id")
    val userId: Int
) : Parcelable