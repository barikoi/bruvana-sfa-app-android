package com.barikoi.cnlapp.Order_Create.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.Callback.OrderListSuccessListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_confirm_order.*
import kotlinx.android.synthetic.main.fragment_confirm_order.bodyLayout
import kotlinx.android.synthetic.main.fragment_confirm_order.btn_tryAgain
import kotlinx.android.synthetic.main.fragment_confirm_order.no_route_check
import kotlinx.android.synthetic.main.fragment_create_order.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class ConfirmOrderFragment : Fragment(), OnEditOrderListener, OrderListSuccessListener {

    var recylerView: RecyclerView? = null
    var progressBar: ProgressBar? = null
    lateinit var ACTIVITY: MainActivity
    var token : String? = null
    var user_id : String? = null
    var sr_id : String? = null
    var route_id: String? = null
    var confirmOrder: AppCompatButton? = null
    var downloadChalan: AppCompatButton? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var appDatabase: AppDatabase? = null
    private var listener: OnEditOrderListener? = null
    val orderList: ArrayList<OrderList> = ArrayList()
    lateinit var adapter: ConfirmOrderListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //checkforOrders()
        //progressBar!!.visibility =View.VISIBLE
        checkforOrders(queue!!, token!!, user_id!!, route_id!!)
        /*adapter = ConfirmOrderListAdapter(orderList, listener!!, "confirm")
        recylerView!!.adapter = adapter
        adapter.notifyDataSetChanged()*/

    }
    companion object{
        var mCallback: OrderListSuccessListener? = ConfirmOrderFragment()
        fun checkforOrders(queue: RequestQueue, token: String, user_id: String, route_id: String/*, listener: OrderListSuccessListener*/) {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            if (mCallback!= null) {
                getAllOrders(Api.get_saved_order+"?user_id="+user_id+/*"&route_id="+route_id+*/"&start_date="+today+" 00:00:00"+"&end_date="+today+" 23:59:59"+"&order_status=SAVED", queue, token, mCallback!!)
            }

        }

        fun getAllOrders(url: String, queue: RequestQueue, token: String, callback: OrderListSuccessListener) {
            ApiServices.apiGET(url, queue, token, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null){
                            val obj = JSONObject(response)
                            val orderArray = obj.getJSONArray("orders")
                            callback.onSuccess(orderArray)

                        }
                    }catch (e: Exception){
                        //progressBar.visibility = View.GONE
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
                    callback.onFailure(error)
                }

                override fun onException(e: Exception) {
                }

            })
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_confirm_order, container, false)

        confirmOrder = view.findViewById(R.id.btnConfirm)
        downloadChalan = view.findViewById(R.id.btndownloadChalan)
        recylerView = view.findViewById(R.id.orderListView)
        progressBar = view.findViewById(R.id.progressBar2)
        confirmOrder!!.setOnClickListener {
            ViewUtils.viewDialog(mContext!!, mContext!!.resources.getString(R.string.confirm_order_dialog), object :
                DialogListener {
                override fun onConfirmed() {
                    createOrder()
                }
                override fun onCanceled() {

                }

            })
        }

        downloadChalan!!.setOnClickListener {
            if (orderList.size> 0) {
                var orderIDs = ""
                for (i in 0 until orderList.size){
                    if (orderIDs.length == 0){
                        orderIDs = orderList[i].orderId
                    }else{
                        orderIDs = orderIDs+","+ orderList[i].orderId
                    }
                }
                if (orderIDs.length > 0) {
                    ApiServices.apiGETInputStream(
                        Api.get_chalan_download + "?order_no=" + orderIDs,
                        queue!!,
                        mContext!!,
                        object : ApiServiceListener {
                            override fun onResponseSuccess(response: String) {
                                TODO("Not yet implemented")
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
                                e.printStackTrace()
                            }

                        })
                }
            }else{
                Toast.makeText(mContext, resources.getString(R.string.no_order_to_download_chalan), Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun createOrder(){
        if (orderList.size> 0){
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val cal = Calendar.getInstance()
            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DATE, 1)
            val nextDay = df.format(cal.time)

            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            for(i in 0 until orderList.size){
                val orderObj = JSONObject()
                orderObj.put("outlet_id", orderList[i].outletId)
                orderObj.put("order_no", orderList[i].orderId)
                orderObj.put("user_id", user_id)
                orderObj.put("employee_id", sr_id)
                /*orderObj.put("ordered_at", today)*/
                /*orderObj.put("delivered_at", nextDay)*/
                /*orderObj.put("distributor_office_code", selectedShop!!.distributor_office_code)*/
                orderObj.put("total_ordered_amount", orderList[i].grandTotal)
                orderObj.put("total_ordered_quantity", orderList[i].totalQuantity)
                orderObj.put("order_status", "PENDING")
                /*orderObj.put("longitude", orderList[i].longitude)
                orderObj.put("latitude", orderList[i].latitude)*/
                val brandsArray = JSONArray()
                val brandList = orderList[i].brands_array
                for (j in 0 until brandList.size){
                    val brandObj = JSONObject()
                    if (brandList[j].ordered_quantity > 0) {
                        brandObj.put("product_id", brandList[j].product_id)
                        /*brandObj.put("brand_id", addedProducts!![j].brand_id)*/
                        brandObj.put("product_code", brandList[j].product_code)
                        brandObj.put("product_name", brandList[j].product_name)
                        brandObj.put("brand_id", "")
                        brandObj.put("sku_code", brandList[j].sku_code)
                        brandObj.put("unit_id", brandList[j].unit_id)
                        brandObj.put("unit_name", brandList[j].unit_name)
                        brandObj.put("unit_code", brandList[j].unit_code)
                        brandObj.put("unit_price", brandList[j].unit_price.toString())
                        brandObj.put("discounted_unit_price", brandList[j].discounted_unit_price.toString())
                        brandObj.put("category_id", brandList[j].category_id)
                        brandObj.put("category_name", brandList[j].category_name)
                        brandObj.put("category_code", brandList[j].category_code)
                        brandObj.put("ordered_quantity", brandList[j].ordered_quantity.toString())
                        brandObj.put("delivered_quantity", "0")
                        brandObj.put("bounced_quantity", "0")
                        brandObj.put("ordered_amount", brandList[j].ordered_total_price.toString())
                        brandObj.put("delivered_amount", "0")
                        brandObj.put("bounced_amount", "0")
                        brandsArray.put(brandObj)
                    }

                }

                orderObj.put("products", brandsArray)
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
        ApiServices.apiJSONObjectPOST(Api.update_saved_order, queue!!, token!!, orderObj, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                TODO("Not yet implemented")
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                try {
                    Log.d("ConfirmOrder", "response api: "+response)
                    appDatabase!!.orderListDao().deleteALL()
                    appDatabase!!.saveOrderDao().deleteALL()
                    val message = response.getString("message")
                    ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                        override fun onConfirmed() {
                            //CreateOrderFragment.setCurrentFragment(ConfirmOrderFragment(), ACTIVITY)
                            //CreateOrderFragment.viewPager!!.setCurrentItem(1)
                            checkforOrders(queue!!, token!!, user_id!!, route_id!!/*, mCallback!!*/)
                        }

                        override fun onCanceled() {
                            TODO("Not yet implemented")
                        }

                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
        route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this
        ACTIVITY = context as MainActivity
        mCallback = this

    }

    override fun onEdit(order: OrderList) {
        editor!!.putString(Api.SELECTED_SHOP_ID, order.outletId)
        editor!!.commit()
        val frag: CreateOrderFragment? = this.parentFragment as CreateOrderFragment?
        CreateOrderFragment.startFragmentWithValue(
            "Order",
            order,
            /*ProductSelectFragment(),*/
            SelectDokanFragment(),
            ACTIVITY
        )

    }

    override fun onSuccess(orderArray: JSONArray) {
        val badgeDrawable = CreateOrderFragment.tabBadge!!.orCreateBadge

        if (orderArray.length() > 0){
            badgeDrawable.number = orderArray.length()
            badgeDrawable.backgroundColor = mContext!!.resources.getColor(R.color.cnl_color_2)
            badgeDrawable.setVisible(true)
            badgeDrawable.maxCharacterCount = 3
            progressBar!!.visibility = View.GONE
            orderList.clear()
            //viewpagertab!!.getTabAt(1)!!.badge!!.number = 3
            no_route_check.visibility = View.GONE
            bodyLayout.visibility = View.VISIBLE
            for (i in 0 until orderArray.length()){
                val orderObj = orderArray.getJSONObject(i)
                if (orderObj.getString("order_status").equals("SAVED", true)){
                    val brandArray = orderObj.getJSONArray("products")
                    tvRouteName.setText(orderObj.getString("route_name"))
                    val productItems: ArrayList<Products> = ArrayList()
                    if (brandArray.length() > 0){
                        for (j in 0 until brandArray.length()){
                            val brandObj = brandArray.getJSONObject(j)
                            if (brandObj.getInt("ordered_quantity") > 0) {
                                productItems.add(
                                    Products(
                                        brandObj.getString("product_id"),
                                        brandObj.getString("product_name"),
                                        brandObj.getString("product_code"),
                                        /*brandObj.getString("brand_id"),
                                        "",*/
                                        brandObj.getDouble("unit_price"),
                                        brandObj.getDouble("discounted_unit_price"),
                                        brandObj.getString("sku_code"),
                                        /*0.0,*/ "",
                                        brandObj.getString("unit_id"),
                                        brandObj.getString("unit_name"),
                                        brandObj.getString("unit_code"),
                                        brandObj.getString("category_id"),
                                        brandObj.getString("category_name"),
                                        brandObj.getString("category_code"),
                                        0, 0,
                                        brandObj.getInt("bounced_quantity"),
                                        brandObj.getInt("ordered_quantity"),
                                        brandObj.getDouble("ordered_amount")
                                    )
                                )
                            }
                        }
                    }
                    orderList.add(
                        OrderList(
                            null,
                            orderObj.getString("order_no"),
                            orderObj.getString("ordered_at"),
                            orderObj.getString("order_status"),
                            orderObj.getString("outlet_id"),
                            orderObj.getString("outlet_name"),
                            orderObj.getString("route_id"),
                            orderObj.getString("route_name"),
                            /*orderObj.getString("distributor_office_code"),*/
                            orderObj.getString("total_ordered_amount"),
                            orderObj.getString("total_ordered_quantity"),
                            orderObj.getString("latitude"),
                            orderObj.getString("longitude"),
                            productItems
                        )
                    )
                }
            }

            orderList.sortByDescending {
                it.orderId
            }
        }else{
            no_route_check.visibility = View.VISIBLE
            bodyLayout.visibility = View.GONE
            badgeDrawable.setVisible(false)
            progressBar!!.visibility = View.GONE
            orderList.clear()
            btn_tryAgain.setOnClickListener {
                checkforOrders(queue!!, token!!, user_id!!, route_id!!/*, mCallback!!*/)
            }

        }


        recylerView.apply {
            adapter = ConfirmOrderListAdapter(orderList, listener!!, "confirm")
            recylerView!!.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    override fun onFailure(error: VolleyError) {
        try {
            ViewUtils.getErrorResponse(error, mContext!!)
            progressBar!!.visibility = View.GONE
        }catch (e: Exception){

        }
    }

    override fun onDataSet(StartDate: Date, EndDate: Date) {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Order", "onResume Confirm")
    }
}