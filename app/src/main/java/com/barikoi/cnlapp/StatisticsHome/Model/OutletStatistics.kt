package com.barikoi.cnlapp.StatisticsHome.Model

import com.barikoi.cnlapp.Model.Products
import java.io.Serializable

class OutletStatistics (
    val shop_id: String,
    val shop_name: String,
    val shop_code: String,
    val category: String,
    val lastOrderDate: String,
    val products: ArrayList<ProductStatistics>
        ):Serializable