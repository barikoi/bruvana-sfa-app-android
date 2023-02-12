package com.barikoi.cnlapp.Model

import java.io.Serializable

class Products(
    var product_id: String,
    val product_name: String,
    val product_code: String,
    val unit_price: Double,
    val discounted_unit_price: Double,
    val sku_code: String,
    /*val discount: Double,*/
    val imageUrl: String,
    val unit_id: String,
    val unit_name: String,
    val unit_code: String,
    val category_id: String,
    val category_name: String,
    val category_code: String,
    val quantity_last_month: Int,
    var stock_available: Int,
    var bounced_quantity: Int,
    var ordered_quantity: Int,
    var ordered_total_price: Double
): Serializable
