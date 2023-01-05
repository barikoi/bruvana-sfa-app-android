package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

class ProductStatistics (
    var product_id: String,
    val product_name: String,
    val product_type: String,
    /*val brand_id: String,*/
    val unit_price: Double,
    var quantity: Int,
    var bounced_quantity: Int,
    var total_price: Double
): Serializable