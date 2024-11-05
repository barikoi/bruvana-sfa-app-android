package com.barikoi.cnlapp.data.remote.models.request


import com.google.gson.annotations.SerializedName

data class TraceLoginBody(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class TraceGroupBody(
    @SerializedName("group_ids")
    val groupId: List<String>,
)