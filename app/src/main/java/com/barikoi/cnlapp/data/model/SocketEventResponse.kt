package com.barikoi.cnlapp.data.model


import com.google.gson.annotations.SerializedName

data class SocketEventResponse(
    @SerializedName("data")
    val liveUser: LiveUser
)


data class LiveUser(
    @SerializedName("active_status")
    val activeStatus: Int,
    @SerializedName("altitude")
    val altitude: String,
    @SerializedName("bearing")
    val bearing: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("gpx_time")
    val gpxTime: String,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("speed")
    val speed: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int
)