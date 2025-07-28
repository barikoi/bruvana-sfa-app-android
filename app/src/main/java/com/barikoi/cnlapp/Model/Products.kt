package com.barikoi.cnlapp.Model

import com.barikoi.cnlapp.data.remote.models.offer.Offer
import java.io.Serializable

data class Products(
    var productId: String,
    val productName: String,
    val productCode: String,
    val unitPrice: Double,
    val discountedUnitPrice: Double,
    val skuCode: String,
    val imageUrl: String,
    val unitId: String,
    val unitName: String,
    val unitCode: String,
    val categoryId: String,
    val categoryName: String,
    val categoryCode: String,
    val quantityLastMonth: Int,
    var stockAvailable: Int,
    var bouncedQuantity: Int,
    var orderedQuantity: Int,
    var orderedTotalPrice: Double,
    var offerID: String? = null,
    val offers: List<Offer> = emptyList()
) : Serializable
