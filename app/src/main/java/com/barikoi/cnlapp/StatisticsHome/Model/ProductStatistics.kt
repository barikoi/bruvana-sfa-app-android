package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

class ProductStatistics(
    var product_id: String,
    val product_name: String,
    val product_code: String,
    val sku_code: String,
    val category_code: String,
    val category_name: String,
    val category_id: String,
    val unit_name: String,
    val unit_id: String,
    val unit_code: String,
    val unit_price: Double,
    val discounted_unit_price: Double,
    var ordered_price: Double,
    var ordered_quantity: Int,
    var quantity: Int,
    var bounced_quantity: Int,
    var total_price: Double
): Serializable