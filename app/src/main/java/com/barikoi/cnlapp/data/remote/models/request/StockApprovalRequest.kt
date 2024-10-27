package com.barikoi.cnlapp.data.remote.models.request


import com.google.gson.annotations.SerializedName

data class StockApprovalRequest(
    @SerializedName("new_stock_data")
    val newStockData: List<NewStockData>?,
    @SerializedName("remarks")
    val remarks: String,
    @SerializedName("status")
    val status: String
)

data class NewStockData(
    @SerializedName("current_available_stock")
    val currentAvailableStock: Int,
    @SerializedName("db_house_id")
    val dbHouseId: String,
    @SerializedName("id")
    val id: Int,

    @SerializedName("status")
    val status: Int,
    @SerializedName("approve_stock")
    val approveStock: Int,
    @SerializedName("product_name")
    val productName: String
)