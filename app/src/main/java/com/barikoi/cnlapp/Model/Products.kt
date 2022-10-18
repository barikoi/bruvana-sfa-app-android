package com.barikoi.cnlapp.Model

import java.io.Serializable

class Products(
    var product_id: String,
    val product_name: String,
    val product_code: String,
    val brand_id: String,
    val unit_price: Double,
    val discount: Double,
    val imageUrl: String,
    val unit_name: String,
    val category_name: String,
    val brand_name: String,
    val quantity_last_month: Int,
    val stock_available: Int
): Serializable
