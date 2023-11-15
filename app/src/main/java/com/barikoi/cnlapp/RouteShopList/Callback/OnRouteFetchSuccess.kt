package com.barikoi.cnlapp.RouteShopList.Callback

import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Markets
import com.barikoi.cnlapp.Model.Routes

interface OnRouteFetchSuccess {

    fun onSuccess(routeName: ArrayList<String>, routes: ArrayList<Routes>?,markets: ArrayList<Markets>)

    fun onError(error: String)
}