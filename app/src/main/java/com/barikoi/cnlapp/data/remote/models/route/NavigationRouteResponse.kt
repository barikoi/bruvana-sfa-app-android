package com.barikoi.cnlapp.data.remote.models.route


import com.google.gson.annotations.SerializedName

data class NavigationRouteResponse(
    @SerializedName("code")
    val code: String,
    @SerializedName("routes")
    val routes: List<Route>,
    @SerializedName("waypoints")
    val waypoints: List<Waypoint>
)