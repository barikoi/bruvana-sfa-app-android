package com.barikoi.cnlapp.Order_Create.Callback

import com.android.volley.VolleyError
import org.json.JSONArray
import java.util.Date

interface OrderListSuccessListener {
    fun onSuccess(array: JSONArray)
    fun onFailure(error: VolleyError)
    fun onDataSet(StartDate: Date, EndDate: Date)
}