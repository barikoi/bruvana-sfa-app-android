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

    val base_url = "http://cnl.bkoih3.ml:7000/api/v1/"

    val loginurl = base_url + "login"
    var logouturl = base_url + "logout"
    val route_outlet_list = base_url+"route-list"
    val verified_shop_list = base_url+"verified-outlets"

    val routes_withfilter = base_url+"routes"
    val all_product_list = base_url+"products"
    val confirm_order = base_url+"create-order"
}