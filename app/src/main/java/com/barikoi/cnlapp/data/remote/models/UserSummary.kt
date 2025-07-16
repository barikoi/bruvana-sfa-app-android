package com.barikoi.cnlapp.data.remote.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSummary(
    val id: String,
    val name: String,
    val userType: String,
    val totalOrders: Double,
    val ads: Double,
    val territoryId: Int,
    val active: Int,
    val inactive: Int
) : Parcelable

