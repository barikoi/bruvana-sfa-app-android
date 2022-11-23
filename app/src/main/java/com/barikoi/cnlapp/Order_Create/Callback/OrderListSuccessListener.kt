package com.barikoi.cnlapp.Order_Create.Callback

import com.android.volley.VolleyError
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import org.json.JSONArray
import java.util.*

interface OrderListSuccessListener {
    fun onSuccess(array: JSONArray)
    fun onFailure(error: VolleyError)
    fun onDataSet(StartDate: Date, EndDate: Date)
}