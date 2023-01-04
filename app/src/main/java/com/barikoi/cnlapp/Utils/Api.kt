package com.barikoi.cnlapp.Utils

import com.barikoi.cnlapp.R

object Api {

    val USER_ID = "user_id"
    val USER_TYPE = "user_type"
    val NAME = "name"
    val SR_CODE = "sr_code"
    val EMPLOYEE_ID = "employee_id"
    val TERRITORY_ID = "territory_id"
    val PHONE = "phone"
    val EMAIL = "email"
    val TOKEN = "token"
    val USER_RANK = "user_rank"

    val SELECTED_SHOP = "selected_shop"
    val SELECTED_SHOP_ID = "selected_shop_id"
    val SELECTED_ROUTE_ID = "selected_route_id"
    val SELECTED_ROUTE_NAME = "selected_route_name"
    val ORDERED_ROUTE_ID = "ordered_route_id"

    val ROUTE_PAGE_SELECTED = "route_page_selected"

    val START_DATE_ATTENDANCE = "start_date_attendance"
    val END_DATE_ATTENDANCE = "end_date_attendance"
    val TOTAL_PRESENT = "total_present"
    val TOTAL_ABSENT = "total_absent"
    val TOTAL_LATE = "total_late"

    val START_DATE_ORDER = "start_date_order"
    val END_DATE_ORDER = "end_date_order"

    val base_url = CNLApp.appContext.resources.getString(R.string.url_base)

    val reverseGeo = "https://barikoi.xyz/v2/api/search/reverse/geocode"
    val APIKEY = "NDI5MzpIQzBBRjZFQ1ZF"

    val loginurl = base_url + "api/v1/login"
    val logouturl = base_url + "api/v1/logout"
    val authUserCheck = base_url+ "api/v1/auth/user"
    //val route_outlet_list = base_url+"api/v1/route-list"
    val verified_shop_list = base_url+"api/v1/outlets"

    /*order create*/
    val routes_withfilter = base_url+"api/v1/routes"
    val shops_byroutes = base_url+"api/v1/outlets"
    val all_product_list = base_url+"api/v1/products"
    val no_order = base_url+"api/v1/no-orders"
    val previous_order = base_url+"api/v1/previous-orders"
    val get_saved_order = base_url+"api/v1/orders"
    val get_orders_to = base_url+"api/v1/orders"
    val confirm_order = base_url+"api/v1/create-order"
    val update_saved_order = base_url+"api/v1/update-orders"

    /*notice*/
    val get_notice = base_url+"api/v1/notices"
    val submit_notice = base_url+"api/v1/notices"

    /*attendance*/
    val create_attendance = base_url+"api/v1/attendance"
    val get_attendance = base_url+"api/v1/get-attendance"

    /*home view*/
    val get_summary = base_url+"api/v1/get-overview-stats"
    //val get_last_week_products = base_url+"api/v1/products"
    val get_last_week_category = base_url+"api/v1/get-category-outlets"
    val get_last_week_delivery_bounce = base_url+"api/v1/get-delivered-bounce-outlets"

    /*TO Part*/
    val get_all_so_list = base_url+"api/v1/get-so"
    val get_dh_list = base_url+"api/v1/dh"

    /*chalan download*/
    val get_chalan_download = base_url+"api/memo-generate"
}