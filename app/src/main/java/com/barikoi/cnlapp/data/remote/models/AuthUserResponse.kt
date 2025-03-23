package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class AuthUserResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("user")
    val user: AuthUser?
)


data class AuthUser(
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("db_house")
    val dbHouse: String?,
    @SerializedName("designation")
    val designation: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: Any,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("so_ranking")
    val soRanking: Int,
    @SerializedName("supervisor_id")
    val supervisorId: Any,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_name")
    val userName: String,

    @SerializedName("group_name")
    val groupName: String?,

    @SerializedName("group_id")
    val groupId: String?
)


data class Designation(
    @SerializedName("code")
    val code: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("sales_type_id")
    val salesTypeId: Int,
    @SerializedName("salestype")
    val salesType: SalesType,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String
)


data class SalesType(
    @SerializedName("code")
    val code: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("updated_at")
    val updatedAt: String?
)