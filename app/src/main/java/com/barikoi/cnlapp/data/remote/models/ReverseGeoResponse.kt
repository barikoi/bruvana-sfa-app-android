package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class ReverseGeoResponse(
    @SerializedName("place")
    val place: Place,
    @SerializedName("status")
    val status: Int
)

data class Place(
    @SerializedName("address")
    val address: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("distance_within_meters")
    val distanceWithinMeters: Double,
    @SerializedName("id")
    val id: Int
)