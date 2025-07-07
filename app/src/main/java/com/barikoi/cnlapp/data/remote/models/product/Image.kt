package com.barikoi.cnlapp.data.remote.models.product


import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("image_url")
    val imageUrl: String
)