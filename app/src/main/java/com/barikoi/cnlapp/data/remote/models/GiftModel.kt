package com.barikoi.cnlapp.data.remote.models

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

@Parcelize
data class GiftModel(
    val title: String,
    val gifts: List<Gift>
) : Parcelable

fun String.giftModelList(): List<GiftModel> {
    val type = object : TypeToken<List<GiftModel>>() {}.type
    return Gson().fromJson(this, type) ?: emptyList()
}
