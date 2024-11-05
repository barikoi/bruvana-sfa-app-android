package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class TraceUserResponse(
    @SerializedName("data")
    val group: Group,
    @SerializedName("status")
    val status: String
)

data class Group(
    @SerializedName("company")
    val company: String,
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("group_users")
    val groupUsers: List<GroupUser>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("updated_at")
    val updatedAt: String
)


data class GroupUser(
    @SerializedName("companies")
    val companies: List<Any>,
    @SerializedName("company")
    val company: Any,
    @SerializedName("company_id")
    val companyId: Int,
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
    @SerializedName("is_allowed")
    val isAllowed: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("position_updated_at")
    val positionUpdatedAt: String,
    @SerializedName("reset_password_token")
    val resetPasswordToken: Any,
    @SerializedName("roles")
    val roles: List<String>,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_last_lat")
    val userLastLat: Double,
    @SerializedName("user_last_lon")
    val userLastLon: Double,
    @SerializedName("user_type")
    val userType: String
)