package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class SoResponseX(
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("users")
    val users: List<SoUser>
)

data class SoUser(
    @SerializedName("designation")
    val designation: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("user_name")
    val userName: String
)