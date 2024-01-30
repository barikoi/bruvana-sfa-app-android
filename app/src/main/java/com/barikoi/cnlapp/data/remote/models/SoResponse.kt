package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class SoResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("so_list")
    val soList: List<So>,
    @SerializedName("status_code")
    val statusCode: Int
)

data class So(
    @SerializedName("id")
    val id: Int,
    @SerializedName("number_of_memo")
    val numberOfMemo: Int,
    @SerializedName("order_amount")
    val orderAmount: Int,
    @SerializedName("sales_officers")
    val salesOfficers: List<SalesOfficer>,
    @SerializedName("sku_per_memo")
    val skuPerMemo: Int,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("user_name")
    val userName: String
)


data class SalesOfficer(
    @SerializedName("designation")
    val designation: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("num_of_memo")
    val numOfMemo: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("productive_outlets")
    val productiveOutlets: Int,
    @SerializedName("so_ordered_value")
    val soOrderedValue: Int,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: Int,
    @SerializedName("total_outlets")
    val totalOutlets: Int,
    @SerializedName("trace_id")
    val traceId: String,
    @SerializedName("user_name")
    val userName: String
)