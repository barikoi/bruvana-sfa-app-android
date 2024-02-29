package com.barikoi.cnlapp.socket.model


import com.google.gson.annotations.SerializedName

data class SocketResponse(
    @SerializedName("user_name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("accuracy")
    val accuracy: Double,
    @SerializedName("altitude")
    val altitude: Double,
    @SerializedName("bearing")
    val bearing: Double,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("gpx_time")
    val gpxTime: String,
    @SerializedName("_id")
    val id: String,
    @SerializedName("is_offline_data")
    val isOfflineData: Boolean,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("location")
    val location: Location,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("speed")
    val speed: Double,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user")
    val user: String,
    @SerializedName("__v")
    val v: Int
)

data class Location(
    @SerializedName("coordinates")
    val coordinates: List<Double>,
    @SerializedName("type")
    val type: String
)