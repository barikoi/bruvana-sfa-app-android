package com.barikoi.cnlapp.data.remote.models.delivery


import com.google.gson.annotations.SerializedName

data class DBSoResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("users")
    val users: List<User>
)