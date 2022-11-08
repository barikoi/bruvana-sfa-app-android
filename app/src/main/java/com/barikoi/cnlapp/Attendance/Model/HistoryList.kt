package com.barikoi.cnlapp.Attendance.Model

import java.io.Serializable

class HistoryList (
        val attendanceId: String,
        val enterTime: String,
        val exitTime: String,
        val isLate: Int,
        val isAbsent: Int,
        val checkInAddress: String,
        val latitude: Double,
        val longitude: Double,
        val imageLink: String,
        val lateReason: String,
        val routeId: Int,
        val routeName: String
        ):Serializable