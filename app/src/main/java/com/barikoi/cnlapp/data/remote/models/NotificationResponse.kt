package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("notifications")
    val notifications: List<Notification>,
    @SerializedName("status_code")
    val statusCode: Int
)


data class Notification(
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("read_at")
    val readAt: String?,
    @SerializedName("title")
    val title: String,
    @SerializedName("updated_at")
    val updatedAt: String
)