package com.barikoi.cnlapp.utils

import com.barikoi.cnlapp.R

object Api {

    const val USER_ID = "user_id"
    const val USER_TYPE = "user_type"
    const val NAME = "name"
    const val SR_CODE = "sr_code"
    const val EMPLOYEE_ID = "employee_id"
    const val TERRITORY_ID = "territory_id"
    const val PHONE = "phone"
    const val EMAIL = "email"
    const val TOKEN = "token"
    const val TRACE_TOKEN = "trace_token"
    const val TRACE_LOGIN_LAST = "TRACE_LOGIN_LAST"
    const val TRACE_USER_ID = "TRACE_USER_ID"
    const val USER_RANK = "user_rank"
    const val TRACE_GROUP_NAME = "trace_group_name"
    const val TRACE_GROUP_ID = "trace_group_id"

    const val SELECTED_SHOP = "selected_shop"
    const val SELECTED_SHOP_ID = "selected_shop_id"
    const val SELECTED_ROUTE_ID = "selected_route_id"
    const val SELECTED_ROUTE_NAME = "selected_route_name"
    const val SELECTED_MARKET_ID = "selected_market_id"
    const val SELECTED_MARKET_NAME = "selected_market_name"
    const val SELECTED_ROUTE_ID_LIST = "selected_route_id_list"
    const val SELECTED_ROUTE_NAME_LIST = "selected_route_name_list"
    const val SELECTED_MARKET_ID_LIST = "selected_market_id_list"
    const val SELECTED_MARKET_NAME_LIST = "selected_market_name_list"
    const val ORDERED_ROUTE_ID = "ordered_route_id"

    const val ROUTE_PAGE_SELECTED = "route_page_selected"

    const val START_DATE_ATTENDANCE = "start_date_attendance"
    const val END_DATE_ATTENDANCE = "end_date_attendance"
    const val TOTAL_PRESENT = "total_present"
    const val TOTAL_ABSENT = "total_absent"
    const val TOTAL_LATE = "total_late"

    const val START_DATE_ORDER = "start_date_order"
    const val END_DATE_ORDER = "end_date_order"

    private val base_url = CNLApp.appContext.resources.getString(R.string.url_base)

    const val reverseGeo = "https://barikoi.xyz/v2/api/search/reverse/geocode"
    const val distance = "https://barikoi.xyz/v1/api/distance/"
    const val APIKEY = "NDI5MzpIQzBBRjZFQ1ZF"
    const val APIKEY_V2 = "bkoi_b13813ee4c57d7073ab4c1a9486a2e0ea0d643b1c3b8fd9155ef8745d3fe3549"

    val loginurl = base_url + "api/v1/login"
    val logouturl = base_url + "api/v1/logout"
    val authUserCheck = base_url + "api/v1/auth/user"
   const val TRACE_BASE_URL = "https://tracev2.barikoimaps.dev/"
    val traceLogin = "https://tracev2.barikoimaps.dev/auth/login"
    val traceAuthCheck = "https://tracev2.barikoimaps.dev/auth/user"
    val traceLogout = "https://backend.barikoi.com:8888/api/v1/dashboard/logout"
    //val route_outlet_list = base_url+"api/v1/route-list"

    /*order create*/
    val routes_withfilter = base_url + "api/v1/routes"
    val all_product_list = base_url + "api/v1/products"
    val lowstock_product_list = base_url + "api/v1/low-stocks"
    val verified_shop_list = base_url + "api/v1/outlets"
    val no_order = base_url + "api/v1/no-orders"
    val previous_order = base_url + "api/v1/previous-orders"
    val get_saved_order = base_url + "api/v1/orders"
    val confirm_order = base_url + "api/v1/create-order"
    val update_saved_order = base_url + "api/v1/update-orders"

    /*notice*/
    val get_notice = base_url + "api/v1/notices"
    val submit_notice = base_url + "api/v1/notices"

    /*attendance*/
    val check_in = base_url + "api/v1/attendance"
    val check_out = base_url + "api/v1/checkout"
    val get_attendance = base_url + "api/v1/get-attendance"
    val check_today_attendance = base_url + "api/v1/get-attendance-history"

    /*home view*/
    val get_summary = base_url + "api/v1/get-overview-stats"
    val get_last_week_category = base_url + "api/v1/outlet-categories"
    val get_last_week_delivery_bounce = base_url + "api/v1/get-delivered-bounce-outlets"

    /*TO Part*/
    val get_all_so_list = base_url + "api/v1/get-so"
    val get_dh_list = base_url + "api/v1/db-houses"

    /*chalan download*/
    val get_chalan_download = base_url + "api/v1/memo-generate"

    /*create shop*/
    val get_shop_type = base_url + "api/v1/outlet-type"
    val get_category_outlet = base_url + "api/v1/outlet-category"
    val get_market_opportunity = base_url + "api/v1/outlet-market-opportunity"
    val create_shop = base_url + "api/v1/create-outlet"
    val update_shop = base_url + "api/v1/update-outlet"

    /*visit-report*/
    val get_visit_report = base_url + "api/v1/visited-report"
}