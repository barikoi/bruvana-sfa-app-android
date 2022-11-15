package com.barikoi.cnlapp.Utils.ApiService

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import org.json.JSONObject

interface ApiServiceListener {
    fun onResponseSuccess(response: String)
    fun onJSONResponseSuccess(response: JSONObject)
    fun onNetworkResponseSuccess(response: NetworkResponse)
    fun onResponseFailure(error: VolleyError)
    fun onException(e: Exception)
}