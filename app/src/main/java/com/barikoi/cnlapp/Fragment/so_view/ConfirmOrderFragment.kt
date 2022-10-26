package com.barikoi.cnlapp.Fragment.so_view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.so_view.ConfirmOrderListAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.RoomDb.OrderList
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.barikoi.cnlapp.Utils.ViewUtils.showGPSDisabledAlertToUser
import com.barikoi.cnlapp.callback.DialogListener
import com.google.android.gms.location.*
import io.sentry.Sentry
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class ConfirmOrderFragment : Fragment() {

    var recylerView: RecyclerView? = null
    lateinit var ACTIVITY: MainActivity
    var user_id : String? = null
    var confirmOrder: AppCompatButton? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var appDatabase: AppDatabase? = null
    var allorderList: List<OrderList> ? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirm_order, container, false)

        confirmOrder = view.findViewById(R.id.btnConfirm)
        recylerView = view.findViewById(R.id.orderList)

        confirmOrder!!.setOnClickListener {
            ViewUtils.viewDialog(mContext!!, "Are your sure want to confirm today's Order?", object : DialogListener {
                override fun onConfirmed() {
                    createOrder()
                }
                override fun onCanceled() {

                }

            })
        }

        getAllOrdersDB()
        return view
    }

    private fun createOrder(){
        val orderList = appDatabase!!.orderListDao().getAllOrders()
        if (orderList!!.size> 0){
            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            for(i in 0 until orderList.size){
                val orderObj = JSONObject()
                orderObj.put("outlet_id", orderList[i].outletId)
                orderObj.put("sr_id", user_id)
                //orderObj.put("ordered_at", "2022-10-25 09:22:00")
                orderObj.put("distributor_office_code", orderList[i].distOfficeCode)
                orderObj.put("grand_total", orderList[i].grandTotal)
                orderObj.put("longitude", orderList[i].longitude)
                orderObj.put("latitude", orderList[i].latitude)
                val brandsArray = JSONArray()
                val brandList = orderList[i].brands_array
                for (j in 0 until brandList.size){
                    val brandObj = JSONObject()
                    brandObj.put("product_id", brandList[j].product_id)
                    brandObj.put("product", brandList[j].product_name)
                    brandObj.put("brand_id", brandList[j].brand_id)
                    brandObj.put("quantity", brandList[j].ordered_quantity)
                    brandObj.put("unit_price", brandList[j].unit_price)
                    brandObj.put("total_price", brandList[j].ordered_total_price)
                    brandsArray!!.put(brandObj)
                }

                orderObj.put("brands", brandsArray)
                ordersArray.put(orderObj)
            }
            obj1.put("orders", ordersArray)

            if (obj1.length() >0){
                Log.d("ConfirmOrder", "response: "+obj1)
                submitOrder(obj1)
            }
        }
    }

    private fun submitOrder(orderObj: JSONObject) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, Api.confirm_order, orderObj,
            { response ->
                try {
                    Log.d("ConfirmOrder", "response api: "+response)
                    CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }) { error ->
            if (error is NoConnectionError) {
                Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
            }
            if (error != null && error.networkResponse != null) {
                try {
                    //NetworkResponse response = error.networkResponse;
                    val s = String(error.networkResponse.data)
                    Log.d("Verify", "message: $s")
                    val data = JSONObject(s)
                    Log.d("Verify", "message: " + data.getString("message"))
                    Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    throw Exception(data.getString("message"))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    Sentry.captureException(e)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Sentry.captureException(e)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Sentry.captureException(e)
                }
            }
        }
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            40 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(jsonObjectRequest)
    }

    private fun getAllOrdersDB() {
        //allorderList = appDatabase!!.orderListDao().getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)
        allorderList = appDatabase!!.orderListDao().getAllOrders()
        if (allorderList!!.size > 0){
            val adapter = ConfirmOrderListAdapter(allorderList!!)
            recylerView!!.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    fun getLocation(){
        val lm = mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext!!)
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationCallback = object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        if (!location.isFromMockProvider) {
                            //createOrder()
                        } else {
                            Toast.makeText(mContext, "Disable mock location", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            mContext!!.applicationContext,
                            "Location not available $location", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            if (ActivityCompat.checkSelfPermission(
                    mContext!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    mContext!!, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            }
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest, mLocationCallback!!,
                Looper.myLooper()!!
            )
        } else {
            showGPSDisabledAlertToUser(mContext!!)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        user_id = prefs!!.getString(Api.USER_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context

        ACTIVITY = context as MainActivity

    }

}