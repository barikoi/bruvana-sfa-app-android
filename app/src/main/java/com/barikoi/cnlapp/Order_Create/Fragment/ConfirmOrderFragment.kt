package com.barikoi.cnlapp.Order_Create.Fragment

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.barikoi.cnlapp.Utils.ViewUtils.showGPSDisabledAlertToUser
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_confirm_order.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ConfirmOrderFragment : Fragment(), OnEditOrderListener {

    var recylerView: RecyclerView? = null
    lateinit var ACTIVITY: MainActivity
    var user_id : String? = null
    var sr_id : String? = null
    var route_id: String? = null
    var confirmOrder: AppCompatButton? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var appDatabase: AppDatabase? = null
    var allorderList: List<OrderList> ? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var listener: OnEditOrderListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            ViewUtils.viewDialog(mContext!!, mContext!!.resources.getString(R.string.confirm_order_dialog), object :
                DialogListener {
                override fun onConfirmed() {
                    //createOrder()
                }
                override fun onCanceled() {

                }

            })
        }
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = df.format(Calendar.getInstance().time)
        getAllOrders(Api.get_saved_order+"?sr_id=T0102"/*+sr_id*/+"&route_id=152"/*+route_id*/+"&start_date=2022-11-14"/*+today*/+"&end_date=2022-11-14"/*+today*/)
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
                    appDatabase!!.orderListDao().deleteALL()
                    appDatabase!!.saveOrderDao().deleteALL()
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

    private fun getAllOrders(url: String) {

        ApiServices.apiGET(url, queue!!, "", object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val itemList: ArrayList<OrderList> = ArrayList()
                        var productItems: ArrayList<Products> = ArrayList()
                        val obj = JSONObject(response)
                        val orderArray = obj.getJSONArray("orders")
                        if (orderArray.length() > 0){
                            for (i in 0 until orderArray.length()){
                                productItems.clear()
                                val orderObj = orderArray.getJSONObject(i)
                                val brandArray = orderObj.getJSONArray("brands")
                                tvRouteName.setText(orderObj.getString("route_name"))
                                if (brandArray.length() > 0){
                                    for (j in 0 until brandArray.length()){
                                        val brandObj = brandArray.getJSONObject(j)
                                        productItems.add(
                                            Products(
                                                brandObj.getString("product_id"),
                                                brandObj.getString("product"),
                                                "",
                                                brandObj.getString("brand_id"),
                                                "",
                                                brandObj.getDouble("unit_price"),
                                                0.0,"",
                                                brandObj.getString("unit_name"),
                                                "",0,0,
                                                brandObj.getInt("quantity"),
                                                brandObj.getDouble("total_price")
                                            )
                                        )
                                    }

                                }
                                itemList.add(
                                    OrderList(
                                        null,
                                        orderObj.getString("order_no"),
                                        orderObj.getString("orders_status"),
                                        orderObj.getString("outlet_id"),
                                        orderObj.getString("outlet_name"),
                                        orderObj.getString("route_id"),
                                        orderObj.getString("route_name"),
                                        orderObj.getString("distributor_office_code"),
                                        orderObj.getString("grand_total"),
                                        orderObj.getString("latitude"),
                                        orderObj.getString("longitude"),
                                        productItems
                                    )
                                )
                            }
                        }


                        val adapter = ConfirmOrderListAdapter(itemList, listener!!, "confirm")
                        recylerView!!.adapter = adapter
                        adapter.notifyDataSetChanged()


                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, mContext!!)
            }

            override fun onException(e: Exception) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.SR_CODE, "")
        route_id = prefs!!.getString(Api.ORDERED_ROUTE_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this
        ACTIVITY = context as MainActivity

    }

    override fun onEdit(order: OrderList) {
        CreateOrderFragment.startFragmentWithValue(
            "Order",
            order,
            ProductSelectFragment(),
            ACTIVITY
        )
    }

}