package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class StockRequestModel(
    @SerializedName("created_by_employee_id")
    val createdByEmployeeId: String,
    @SerializedName("created_by_user_id")
    val createdByUserId: String,
    @SerializedName("db_house_id")
    val dbHouseId: String,
    @SerializedName("route_id")
    val routeId: String,
    @SerializedName("stocks")
    val stocks: List<Stock?>,
    @SerializedName("type")
    val type: String
)

data class Stock(
    @SerializedName("current_available_stock")
    val currentAvailableStock: Int,
    @SerializedName("id")
    val id: Int,
//    @SerializedName("status")
//    val status: Int,
//    @SerializedName("db_house_id")
//    val dbHouseId: Int,
//    @SerializedName("approve_stock")
//    val approveStock: Int,
//    @SerializedName("product_name")
//    val productName: Int
)