package com.barikoi.cnlapp.Model

import java.io.Serializable

class Products(
    var product_id: String,
    val product_name: String,
    val product_code: String,
    /*val brand_id: String,
    val brand_name: String,*/
    val unit_price: Double,
    /*val discount: Double,*/
    val imageUrl: String,
    val unit_name: String,
    val category_name: String,
    val quantity_last_month: Int,
    var stock_available: Int,
    var bounced_quantity: Int,
    var ordered_quantity: Int,
    var ordered_total_price: Double
): Serializable
