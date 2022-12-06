package com.barikoi.cnlapp.ProductStock.Model

import org.json.JSONArray
import java.io.Serializable

class OrdersSO(
    var so_id: String,
    var so_name: String,
    var order_collected: String,
    var total_outlets: String,
    var total_bounce: Double,
    var ordersArray: JSONArray
):Serializable