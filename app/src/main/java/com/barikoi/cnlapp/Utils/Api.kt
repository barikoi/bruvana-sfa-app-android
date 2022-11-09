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
    val USER_RANK = "user_rank"

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
    val logouturl = base_url + "api/v1/logout"
    val authUserCheck = base_url+ "api/v1/auth/user"
    val route_outlet_list = base_url+"api/v1/route-list"
    val verified_shop_list = base_url+"api/v1/verified-outlets"

    val routes_withfilter = base_url+"api/v1/routes"
    val all_product_list = base_url+"api/v1/products"
    val confirm_order = base_url+"api/v1/create-order"

    val create_attendance = base_url+"api/v1/attendance"
    val get_attendance = base_url+"api/v1/get-attendance"

    val get_summary = base_url+"api/v1/get-overview-stats?start_date=2022-04-09 00:00:00&end_date=2022-10-30 23:59:59&sr_id=3316"
    val get_last_week_products = base_url+"api/v1/products?start_date=2022-04-01&end_date=2022-10-31&so_id=3721&route_id=2"
    val get_last_week_category = base_url+"api/v1/get-categories?start_date=2022-03-01&end_date=2022-03-30&sr_id=4105&route_id=20"
    val get_last_week_delivery = base_url+"api/v1/get-delivered-outlets?start_date=2022-04-01 00:00:00&end_date=2022-11-30 23:59:59&sr_id=3185&route_id=310"
    val get_last_week_bounce = base_url+""
}