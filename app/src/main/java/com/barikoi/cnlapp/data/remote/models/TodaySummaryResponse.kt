package com.barikoi.cnlapp.data.remote.models


import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.RoundingMode

data class TodaySummaryResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("to_list")
    val toList: List<To>
)

data class To(
    @SerializedName("ads")
    val ads: Double,
    @SerializedName("aiv")
    val aiv: Double,
    @SerializedName("bounced_percentage")
    val bouncedPercentage: Double,
    @SerializedName("bounced_amount")
    val bouncedAmount: String,
    @SerializedName("rds")
    val rds: Double,
    @SerializedName("num_of_sku")
    val numOfSku: Int,
    @SerializedName("num_of_visits")
    val numOfVisits: Int,
    @SerializedName("oneToHundred")
    val oneToHundred: Int,
    @SerializedName("to_id")
    val toId: Int,
    @SerializedName("user_name")
    val toName: String,
    @SerializedName("total_bounced_amount")
    val totalBouncedAmount: String,
    @SerializedName("total_ordered_amount")
    val totalOrderedAmount: String,
    @SerializedName("total_orders")
    val totalOrders: Int,
    @SerializedName("unique_outlet_count")
    val uniqueOutletCount: Int,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("activeInactiveData")
    val activeInactiveData: ActiveInactiveData?

) {
    @SuppressLint("DefaultLocale")
    fun totalAmountFormatted(): String {
        return BigDecimal(totalOrderedAmount.toDouble()).setScale(2, RoundingMode.HALF_UP)
            .toString()
    }

    fun toUserSummary(): UserSummary {
        return UserSummary(
            id = toId.toString(),
            name = toName,
            userType = "TO",
            totalOrders = totalOrders.toDouble(),
            ads = BigDecimal(ads).setScale(2, RoundingMode.HALF_UP).toDouble(),
            territoryId = territoryId,
            active = activeInactiveData?.active ?: 0,
            inactive = activeInactiveData?.inactive ?: 0,
        )
    }
}

data class ActiveInactiveData(
    @SerializedName("active")
    val active: Int,
    @SerializedName("inactive")
    val inactive: Int
)