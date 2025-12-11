package com.barikoi.cnlapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class OutletTypeSummaryResponse(
    @SerializedName("data")
    val `data`: List<OutletTypeData>,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("status")
    val status: String
)


data class OutletTypeData(
    @SerializedName("aiv")
    val aiv: String,
    @SerializedName("contribution")
    val contribution: String?,
    @SerializedName("delivery_amount")
    val deliveryAmount: String?,
    @SerializedName("gross_amount")
    val grossAmount: String?,
    @SerializedName("non_productive_visits")
    val nonProductiveVisits: Int,
    @SerializedName("order_amount")
    val orderAmount: String,
    @SerializedName("outlet_type")
    val outletType: String,
    @SerializedName("productive_visits")
    val productiveVisits: Int,
    @SerializedName("qty_presence")
    val qtyPresence: String?,
    @SerializedName("total_outlet")
    val totalOutlet: Int,
    @SerializedName("total_visits")
    val totalVisits: Int,

    var isExpanded: Boolean = false
)