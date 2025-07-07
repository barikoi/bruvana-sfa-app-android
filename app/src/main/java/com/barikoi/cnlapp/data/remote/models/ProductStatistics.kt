package com.barikoi.cnlapp.data.remote.models

data class ProductStatistics(
    var productId: String,
    val productName: String,
    val productCode: String,
    val skuCode: String,
    val categoryCode: String,
    val categoryName: String,
    val categoryId: String,
    val unitName: String,
    val unitId: String,
    val unitCode: String,
    val unitPrice: Double,
    val discountedUnitPrice: Double,
    var orderedPrice: Double,
    var orderedQuantity: Int,
    var availableQuantity: Int,
    var bouncedQuantity: Int,
    var totalPrice: Double
)
