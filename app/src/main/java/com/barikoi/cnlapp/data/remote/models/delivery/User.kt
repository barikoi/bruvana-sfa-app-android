package com.barikoi.cnlapp.data.remote.models.delivery


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("designation")
    val designation: String,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_name")
    val userName: String
)