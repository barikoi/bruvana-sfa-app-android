package com.barikoi.cnlapp.data.remote.models.route


import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("legs")
    val legs: List<Leg>,
    @SerializedName("weight")
    val weight: Double,
    @SerializedName("weight_name")
    val weightName: String
)