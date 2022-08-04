package com.barikoi.cnlapp.Utils

object Api {

    val USER_ID = "user_id"
    val USER_TYPE = "user_type"
    val NAME = "name"
    val SR_CODE = "sr_code"
    val PHONE = "phone"
    val EMAIL = "email"
    val TOKEN = "token"

    val ROUTE_PAGE_SELECTED = "route_page_selected"

    val base_url = "http://cnl.bkoih3.ml:7000/api/v1/"

    val loginurl = base_url + "login"
    var logouturl = base_url + "logout"
    val route_outlet_list = base_url+"route-list"
}