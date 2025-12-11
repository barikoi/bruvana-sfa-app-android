package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

data class Categories (
    val outletCategory: String,
    val totalOutlet: String,
    val orderDone: String,
    val orderValue: String
): Serializable