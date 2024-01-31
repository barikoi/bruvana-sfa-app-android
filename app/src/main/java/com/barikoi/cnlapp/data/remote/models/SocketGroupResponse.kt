package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class SocketGroupResponse(
    @SerializedName("data")
    val groups: List<SocketGroup>,
    @SerializedName("status")
    val status: Int
)

data class SocketGroup(
    @SerializedName("company_id")
    val companyId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("updated_at")
    val updatedAt: String
)