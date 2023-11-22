package com.barikoi.cnlapp.RouteShopList.Callback

import com.barikoi.cnlapp.Model.Routes

interface OnRouteFetchSuccess {

    fun onSuccess(routeName: ArrayList<String>, routes: ArrayList<Routes>?,markets: ArrayList<Any>)

    fun onError(error: String)
}