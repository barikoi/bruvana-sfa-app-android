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
    val distributor_office: String,
    val distributor_office_code: String,
    val territory_name: String,
    val latitude: Double,
    val longitude: Double,
    val route_code: String,
    val route_name: String,
    val lastOrderDate: String
): Serializable