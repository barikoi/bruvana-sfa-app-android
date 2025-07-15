package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

data class TargetValue(
    val title: String,
    var target: String,
    var targetValue: Double,
    var completed: String,
    var completedValue: Double,
) : Serializable

data class TargetAndCompleted(
    val title: String,
    var target: String,
    var completed: String,
)