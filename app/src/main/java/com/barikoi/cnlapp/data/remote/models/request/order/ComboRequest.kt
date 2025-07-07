package com.barikoi.cnlapp.data.remote.models.request.order


import com.google.gson.annotations.SerializedName

data class ComboRequest(
    @SerializedName("offer_id")
    val offerId: Int,
    @SerializedName("quantity")
    val quantity: Int
)