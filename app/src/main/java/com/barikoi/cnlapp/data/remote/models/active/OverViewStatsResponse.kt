package com.barikoi.cnlapp.data.remote.models.active

import com.google.gson.annotations.SerializedName

data class OverViewStatsResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("target_completed")
    val targetCompleted: List<TargetCompleted>,
    @SerializedName("targets")
    val targets: List<Target>,
    @SerializedName("message")
    val message: String?
)

data class TargetCompleted(
    @SerializedName("ads")
    val ads: Double?,
    @SerializedName("aiv")
    val aiv: String?,
    @SerializedName("bounce_amount_percentage")
    val bounceAmountPercentage: Double,
    @SerializedName("delivered_value")
    val deliveredValue: String?,
    @SerializedName("number_of_memo")
    val numberOfMemo: Int?,
    @SerializedName("number_of_visits")
    val numberOfVisits: Int?,
    @SerializedName("rds")
    val rds: Double?,
    @SerializedName("revenue")
    val revenue: String?,
    @SerializedName("sku_per_memo")
    val skuPerMemo: String?
)

data class Target(
    @SerializedName("target_ads")
    val targetAds: Double,
    @SerializedName("target_aiv")
    val targetAiv: Double,
    @SerializedName("target_amount")
    val targetAmount: Double,
    @SerializedName("target_number_of_memo")
    val targetNumberOfMemo: Int,
    @SerializedName("target_rds")
    val targetRds: String?,
    @SerializedName("target_sku_per_memo")
    val targetSkuPerMemo: Int,
    @SerializedName("threshold_bounce_percentage")
    val thresholdBouncePercentage: Int,
    @SerializedName("target_number_of_visits")
    val targetNumberOfVisits: Int
)