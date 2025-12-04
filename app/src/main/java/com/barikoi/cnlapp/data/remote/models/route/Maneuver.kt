package com.barikoi.cnlapp.data.remote.models.route


import com.google.gson.annotations.SerializedName

data class Maneuver(
    @SerializedName("bearing_after")
    val bearingAfter: Int,
    @SerializedName("bearing_before")
    val bearingBefore: Int,
    @SerializedName("instruction")
    val instruction: String,
    @SerializedName("location")
    val location: List<Double>,
    @SerializedName("modifier")
    val modifier: String,
    @SerializedName("type")
    val type: String
)