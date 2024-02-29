package com.barikoi.cnlapp.data.model.socket


import com.google.gson.annotations.SerializedName

data class SocketGroupUsersResponse(
    @SerializedName("data")
    val data: Data,
    @SerializedName("status")
    val status: String
)


data class Data(
    @SerializedName("company")
    val company: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("group_users")
    val groupUsers: List<GroupUser>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("__v")
    val v: Int
)

data class GroupUser(
    @SerializedName("company")
    val company: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("email_verification_token")
    val emailVerificationToken: Any,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any,
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("position_updated_at")
    val positionUpdatedAt: String,
    @SerializedName("reset_password_token")
    val resetPasswordToken: Any,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_last_lat")
    val userLastLat: Double,
    @SerializedName("user_last_lon")
    val userLastLon: Double,
    @SerializedName("user_type")
    val userType: String,
    @SerializedName("__v")
    val v: Int
)