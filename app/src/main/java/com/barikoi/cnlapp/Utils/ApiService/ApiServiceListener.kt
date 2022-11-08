package com.barikoi.cnlapp.Utils.ApiService

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError

interface ApiServiceListener {
    fun onResponseSuccess(response: String)
    fun onNetworkResponseSuccess(response: NetworkResponse)
    fun onResponseFailure(error: VolleyError)
    fun onException(e: Exception)
}