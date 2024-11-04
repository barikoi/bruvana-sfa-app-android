package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

data class Categories (
    val outlet_category: String,
    val total_outlet: String,
    val order_done: String,
    val order_value: String
): Serializable