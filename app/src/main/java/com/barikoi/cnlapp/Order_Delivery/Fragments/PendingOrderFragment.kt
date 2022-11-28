package com.barikoi.cnlapp.Order_Delivery.Fragments

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.Callback.OrderListSuccessListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Delivery.Adapter.OrderDeliveryListAdapter
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import kotlinx.android.synthetic.main.fragment_pending_order.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PendingOrderFragment : Fragment(), OrderListSuccessListener, OnEditOrderListener {
    var recylerView: RecyclerView? = null
    var progressBar: ProgressBar? = null
    lateinit var ACTIVITY: OrderDeliveryUpdateActivity
    var token : String? = null
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
    val orderList: ArrayList<OrderList> = ArrayList()
    lateinit var adapter: OrderDeliveryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_pending_order, container, false)
        recylerView = view.findViewById(R.id.orderListView)
        progressBar = view.findViewById(R.id.progressBar2)
        return view
    }


    companion object{
        var mCallback: OrderListSuccessListener? = PendingOrderFragment()
        fun checkforOrders(queue: RequestQueue, token: String, sr_id: String, route_id: String, start: String, end: String) {
            if (mCallback!= null) {
                if (sr_id.length == 0){
                    getAllOrders(Api.get_orders_to+"?start_date="+start+"&end_date="+end, queue, token, mCallback!!)
                }else{
                    getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+start+"&end_date="+end, queue, token, mCallback!!)
                }

            }

        }

        fun getAllOrders(url: String, queue: RequestQueue, token: String, callback: OrderListSuccessListener) {
            ApiServices.apiGET(url, queue, token, object : ApiServiceListener {
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

    override fun onSuccess(array: JSONArray) {
        progressBar!!.visibility = View.GONE
        orderList.clear()
        if (array.length() > 0){
            no_route_check.visibility = View.GONE
            bodyLayout.visibility = View.VISIBLE
            for (i in 0 until array.length()){
                val orderObj = array.getJSONObject(i)
                if (orderObj.getString("orders_status").equals("PENDING", true)){
                    val brandArray = orderObj.getJSONArray("brands")
                    tvRouteName.setText(orderObj.getString("route_name"))
                    val productItems: ArrayList<Products> = ArrayList()
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
                    orderList.add(
                        OrderList(
                            null,
                            orderObj.getString("order_no"),
                            orderObj.getString("ordered_at"),
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
        }else{
            no_route_check.visibility = View.VISIBLE
            bodyLayout.visibility = View.GONE

            btn_tryAgain.setOnClickListener {
                /*checkforOrders(
                    queue!!,
                    token!!,
                    sr_id!!,
                    route_id!!
                )*/
            }

        }


        recylerView.apply {
            adapter = OrderDeliveryListAdapter(orderList, listener!!, "confirm")
            recylerView!!.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    override fun onFailure(error: VolleyError) {
        progressBar!!.visibility = View.GONE
        ViewUtils.getErrorResponse(error, mContext!!)
    }

    override fun onDataSet(StartDate: Date, EndDate: Date) {

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
        ACTIVITY = context as OrderDeliveryUpdateActivity
        mCallback = this

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onEdit(order: OrderList) {

        viewDialog(mContext!!, order)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun viewDialog(mContext: Context, order: OrderList){
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.single_order_status_update)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        var statusGroup = dialog.findViewById<RadioGroup>(R.id.status_group)
        val radio_group : RadioGroup? = null
        val itemValue : ArrayList<String> = ArrayList()
        itemValue.add(mContext.resources.getString(R.string.pending))
        itemValue.add(mContext.resources.getString(R.string.delivered))
        itemValue.add(mContext.resources.getString(R.string.bounced))
        statusGroup = RadioGroup(mContext)
        statusGroup.setOrientation(RadioGroup.HORIZONTAL)
        for (i in itemValue.indices) {
            val rbn = RadioButton(mContext)
            rbn.setText(itemValue.get(i))
            rbn.id = i
            rbn.setTextColor(resources.getColor(R.color.white))
            rbn.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            statusGroup.addView(rbn)
        }
        radio_group!!.addView(statusGroup)
        statusGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            /*if (checkedId == 0) {
                isChecked = 1
                announcementType = "LATE"
            } else if (checkedId == 1) {
                isChecked = 1
                announcementType = "LEAVE"
            } else {
                isChecked = 1
                announcementType = "GENERAL"
            }*/
        })

        var dformat = DecimalFormat("#.##")
        outletName.setText(order.outletName)
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
        val orderDate = df.format(oldDate.parse(order.orderedAt))
        tvLastOrderDate.setText(mContext.resources.getString(R.string.last_order_date)+ orderDate)
        tvItemCount.setText(order.brands_array.size.toString()+mContext.resources.getString(R.string.items))

        var grandTotal = 0.0
        if (order.brands_array.size> 0){
            val brandsStatistics : ArrayList<ProductStatistics> = ArrayList()
            for (i in 0 until order.brands_array.size){
                brandsStatistics.add(
                    ProductStatistics(
                        order.brands_array[i].product_id,
                        order.brands_array[i].product_name,
                        order.brands_array[i].category_name,
                        order.brands_array[i].brand_id,
                        order.brands_array[i].unit_price,
                        order.brands_array[i].ordered_quantity,
                        order.brands_array[i].ordered_total_price
                ))
            }
            val adapter = OutletProductAdapter(brandsStatistics)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal.setText(dformat.format(order.grandTotal).toString())

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }
}