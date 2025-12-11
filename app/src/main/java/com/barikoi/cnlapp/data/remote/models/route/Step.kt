package com.barikoi.cnlapp.data.remote.models.route


import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("distance")
    val distance: Double,
    @SerializedName("driving_side")
    val drivingSide: String,
    @SerializedName("duration")
    val duration: Double,
    @SerializedName("geometry")
    val geometry: Geometry,
    @SerializedName("intersections")
    val intersections: List<Intersection>,
    @SerializedName("maneuver")
    val maneuver: Maneuver,
    @SerializedName("mode")
    val mode: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("ref")
    val ref: String,
    @SerializedName("weight")
    val weight: Double
)