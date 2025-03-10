package com.barikoi.cnlapp.utils

import android.location.Location

object Constants {
    const val TOKEN = "token"
    const val REQUEST_TYPE = "REQUEST_TYPE"
    const val STATUS_DECLINED = "REJECTED"
    const val STATUS_PENDING = "PENDING"
    const val STATUS_APPROVED = "APPROVED"
    const val STATUS_PARTIAL_APPROVED = "PARTIAL_APPROVED"

    const val STOCK = "STOCK"
    const val OUTLET = "OUTLET"

    const val POST_OFFICE_DATA = "postOfficeData"
    const val PREF_NAME = "BPOCapturePref"
    const val API_ERROR_MESSAGE = "Something went wrong. Please try again..."
    const val KEY_TOKEN = "bearerToken"
    const val KEY_IS_LOGIN = "is_login"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_NAME = "userName"
    const val KEY_USER_EMAIL = "userEmail"

    const val CNL_OK_CLIENT = "cnl"
    const val TRACE_OK_CLIENT = "trace"
    const val IMAGE_PICKER = "IMAGE_PICKER"

    var token: String? = null
    var isLoggedIn = false

    const val CUSTOMER_ADDRESS = "CUSTOMER_ADDRESS"

    const val DB_HOUSE_ID = "db_house_id"
    const val AREA_ID = "area_id"
    const val TERRITORY_ID = "territory_id"
    const val REGION_ID = "region_id"
    const val REJECTED = "REJECTED"
    const val APPROVED = "APPROVED"
    const val PARTIAL_APPROVED = "PARTIAL_APPROVED"
    const val NATION_ID = "nation_id"

    const val USER_TO = "TO"
    const val USER_SO = "SO"

    //    const val MOBILE_REGEX = "^(?:(?:\\+|00)88|01)?\\d{11}\$"
    const val MOBILE_REGEX = "^(?:\\+?88|0088)?01[15-9]\\d{8}\$"

    val  shopType = listOf(
        "General Large Store (A)",
        "General Small Store (B)",
        "Mudi Large Outlet (Grocery Store) (A)",
        "Mudi Small Outlet (Grocery Store) (B)",
        "Confectionary/Bakery (B)",
        "Pharmacy (P)",
        "Tea Stall (C)"
    )

    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val startPoint = Location("locationA")
        startPoint.latitude = lat1
        startPoint.longitude = lon1

        val endPoint = Location("locationB")
        endPoint.latitude = lat2
        endPoint.longitude = lon2

        return startPoint.distanceTo(endPoint)
    }
}