package com.barikoi.cnlapp.Model

import java.io.Serializable

class Shops(
    var shop_id: String,
    val shop_name: String,
    val state: String,
    val address: String,
    val shop_code: String,
    val shop_type: String,
    val category: String,
    val shop_owner: String,
    val min_order: String,
    val market_opportunity: String,
    val contact_number: String,
    val is_buyer: Int,
    val imageUrl: String,
    val imageArray: ArrayList<String>,
    val territory_name: String,
    val latitude: Double,
    val longitude: Double,
    val route_code: String,
    val route_name: String,
    val lastOrderDate: String,
    val isVerified: Int,
    val isOrdered: Int,
    val isNoOrdered: Int,
    val distance: Float
): Serializable