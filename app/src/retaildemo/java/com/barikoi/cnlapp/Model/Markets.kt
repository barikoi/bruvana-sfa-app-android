package com.barikoi.cnlapp.Model

import java.io.Serializable

class Markets (
    val route_id: String,
    val route_code: String,
    val route_name: String,
    val market_id: String,
    val market_name: String,
    val territory_name: String,
    val area_name: String,
    val outlet_count: String,
    val shopList: ArrayList<Shops>
    ):Serializable