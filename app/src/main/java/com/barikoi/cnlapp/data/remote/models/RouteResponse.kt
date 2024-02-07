package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("routes")
    val routes: List<Route>,
    @SerializedName("status_code")
    val statusCode: Int
)

data class Route(
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("area_name")
    val areaName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("route_code")
    val routeCode: String,
    @SerializedName("route_name")
    val routeName: String,
    @SerializedName("outlet_count")
    val outletCount: String,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("territory_name")
    val territoryName: String,
    @SerializedName("user_id")
    val userId: Int
)