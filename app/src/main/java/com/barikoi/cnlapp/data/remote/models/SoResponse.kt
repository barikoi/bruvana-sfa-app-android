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
    val orderAmount: Double,
    @SerializedName("sales_officers")
    val salesOfficers: List<SalesOfficer>,
    @SerializedName("sku_per_memo")
    val skuPerMemo: String,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("user_name")
    val userName: String
)


data class SalesOfficer(
    @SerializedName("id")
    val id: Int,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("num_of_memo")
    val numOfMemo: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("productive_outlets")
    val productiveOutlets: Int,
    @SerializedName("so_ordered_value")
    val soOrderedValue: Double,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: Double,
    @SerializedName("total_outlets")
    val totalOutlets: Int,
    @SerializedName("trace_id")
    val traceId: String,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("num_of_visits")
    val numOfVisits: Int,
    @SerializedName("total_orders")
    val totalOrders: Int,
    @SerializedName("ads")
    val ads: String?
) {
    fun toUserSummary(): UserSummary {
        return UserSummary(
            id = id.toString(),
            name = userName,
            userType = designation,
            totalOrders = soOrderedValue,
            numberOfMemos = numOfMemo,
            numberOfVisits = numOfVisits,
            ads = (ads ?: "0.0").toDouble(),
            territoryId = 0,
            active = 0,
            inactive = 0
        )
    }
}