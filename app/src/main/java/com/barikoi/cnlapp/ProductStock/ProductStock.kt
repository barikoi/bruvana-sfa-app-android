package com.barikoi.cnlapp.ProductStock

import java.io.Serializable

class ProductStock(
    val product_id: String,
    val product_name: String,
    val imageUrl: String,
    val sold_quantity: String,
    val per_unit_quantity: String,
    val unit_name: String
):Serializable