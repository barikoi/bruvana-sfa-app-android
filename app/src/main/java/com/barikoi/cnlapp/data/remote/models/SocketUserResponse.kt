package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class SocketUserResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("users")
    val users: List<User>
)

data class User(
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("local_users")
    val localUsers: List<LocalUser>,
    @SerializedName("updated_at")
    val updatedAt: String
)


data class LocalUser(
    @SerializedName("active_status")
    val activeStatus: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("isAllowed")
    val isAllowed: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("position_updated_at")
    val positionUpdatedAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_last_lat")
    val userLastLat: Double,
    @SerializedName("user_last_lon")
    val userLastLon: Double,
    @SerializedName("userType")
    val userType: Int
)