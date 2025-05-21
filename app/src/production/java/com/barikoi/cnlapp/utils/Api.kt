package com.barikoi.cnlapp.utils

import com.barikoi.cnlapp.BuildConfig

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
    const val USER_RANK = "user_rank"

    val SELECTED_SHOP = "selected_shop"
    val SELECTED_SHOP_ID = "selected_shop_id"
    val SELECTED_ROUTE_ID = "selected_route_id"
    val SELECTED_ROUTE_NAME = "selected_route_name"
    val SELECTED_MARKET_ID = "selected_market_id"
    val SELECTED_MARKET_NAME = "selected_market_name"
    val SELECTED_ROUTE_ID_LIST = "selected_route_id_list"
    val SELECTED_ROUTE_NAME_LIST = "selected_route_name_list"
    val SELECTED_MARKET_ID_LIST = "selected_market_id_list"
    val SELECTED_MARKET_NAME_LIST = "selected_market_name_list"
    val ORDERED_ROUTE_ID = "ordered_route_id"

    val ROUTE_PAGE_SELECTED = "route_page_selected"

    val START_DATE_ATTENDANCE = "start_date_attendance"
    val END_DATE_ATTENDANCE = "end_date_attendance"
    val TOTAL_PRESENT = "total_present"
    val TOTAL_ABSENT = "total_absent"
    val TOTAL_LATE = "total_late"

    val START_DATE_ORDER = "start_date_order"
    val END_DATE_ORDER = "end_date_order"


    const val TRACE_GROUP_NAME = "TRACE_GROUP_NAME"
    const val TRACE_GROUP_ID = "TRACE_GROUP_ID"
    const val TRACE_TOKEN = "TRACE_TOKEN"

    val loginurl = BuildConfig.url_base + "api/v1/login"
    val logouturl = BuildConfig.url_base + "api/v1/logout"
    val authUserCheck = BuildConfig.url_base + "api/v1/auth/user"

    /*order create*/
    val routes_withfilter = BuildConfig.url_base + "api/v1/routes"
    val all_product_list = BuildConfig.url_base + "api/v1/products"
    val lowstock_product_list = BuildConfig.url_base + "api/v1/low-stocks"
    val verified_shop_list = BuildConfig.url_base + "api/v1/outlets"
    val no_order = BuildConfig.url_base + "api/v1/no-orders"
    val previous_order = BuildConfig.url_base + "api/v1/previous-orders"
    val get_saved_order = BuildConfig.url_base + "api/v1/orders"
    val confirm_order = BuildConfig.url_base + "api/v1/create-order"
    val update_saved_order = BuildConfig.url_base + "api/v1/update-orders"

    /*notice*/
    val get_notice = BuildConfig.url_base + "api/v1/notices"
    val submit_notice = BuildConfig.url_base + "api/v1/notices"

    /*attendance*/
    val check_in = BuildConfig.url_base + "api/v1/attendance"
    val check_out = BuildConfig.url_base + "api/v1/checkout"
    val get_attendance = BuildConfig.url_base + "api/v1/get-attendance"
    val check_today_attendance = BuildConfig.url_base + "api/v1/get-attendance-history"

    /*home view*/
    val get_summary = BuildConfig.url_base + "api/v1/get-overview-stats"
    val get_last_week_category = BuildConfig.url_base + "api/v1/outlet-categories"
    val get_last_week_delivery_bounce = BuildConfig.url_base + "api/v1/get-delivered-bounce-outlets"

    /*TO Part*/
    val get_all_so_list = BuildConfig.url_base + "api/v1/get-so"
    val get_dh_list = BuildConfig.url_base + "api/v1/db-houses"

    /*chalan download*/
    val get_chalan_download = BuildConfig.url_base + "api/v1/memo-generate"

    /*create shop*/
    val get_shop_type = BuildConfig.url_base + "api/v1/outlet-type"
    val get_category_outlet = BuildConfig.url_base + "api/v1/outlet-category"
    val get_market_opportunity = BuildConfig.url_base + "api/v1/outlet-market-opportunity"
    val create_shop = BuildConfig.url_base + "api/v1/create-outlet"
    val update_shop = BuildConfig.url_base + "api/v1/update-outlet"

    /*visit-report*/
    val get_visit_report = BuildConfig.url_base + "api/v1/visited-report"


    const val reverseGeo = "https://barikoi.xyz/v2/api/search/reverse/geocode"
    const val distance = "https://barikoi.xyz/v1/api/distance/"

    const val traceLogin = "https://backend.barikoi.com:8888/api/v1/dashboard/login"
    const val traceAuthCheck = "https://backend.barikoi.com:8888/api/v1/auth/user"
}