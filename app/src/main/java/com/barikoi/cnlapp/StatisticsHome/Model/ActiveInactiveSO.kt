package com.barikoi.cnlapp.StatisticsHome.Model

import java.io.Serializable

class ActiveInactiveSO (
    val userName: String,
    val userId: String,
    val status: String,
    val lastUpdatedTime: String,
    val checkInAddress: String,
    val latitude: Double,
    val longitude: Double,
    val imageLink: String
): Serializable