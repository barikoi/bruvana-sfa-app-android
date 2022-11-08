package com.barikoi.cnlapp.Utils

object Api {

    val USER_ID = "user_id"
    val USER_TYPE = "user_type"
    val NAME = "name"
    val SR_CODE = "sr_code"
    val EMPLOYEE_ID = "employee_id"
    val PHONE = "phone"
    val EMAIL = "email"
    val TOKEN = "token"

    val SELECTED_SHOP = "selected_shop"
    val SELECTED_SHOP_ID = "selected_shop_id"
    val SELECTED_ROUTE_ID = "selected_route_id"

    val ROUTE_PAGE_SELECTED = "route_page_selected"

    val START_DATE_ATTENDANCE = "start_date_attendance"
    val END_DATE_ATTENDANCE = "end_date_attendance"
    val TOTAL_PRESENT = "total_present"
    val TOTAL_ABSENT = "total_absent"
    val TOTAL_LATE = "total_late"

    val base_url = "http://cnl.bkoih3.ml:7000/"

    val reverseGeo = "https://barikoi.xyz/v2/api/search/reverse/geocode"
    val APIKEY = "NDI5MzpIQzBBRjZFQ1ZF"

    val loginurl = base_url + "api/v1/login"
    var logouturl = base_url + "api/v1/logout"
    val route_outlet_list = base_url+"api/v1/route-list"
    val verified_shop_list = base_url+"api/v1/verified-outlets"

    val routes_withfilter = base_url+"api/v1/routes"
    val all_product_list = base_url+"api/v1/products"
    val confirm_order = base_url+"api/v1/create-order"

    val create_attendance = base_url+"api/v1/attendance"
    val get_attendance = base_url+"api/v1/get-attendance"
}