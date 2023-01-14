package com.barikoi.cnlapp.Order_Delivery.Fragments

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.Callback.OrderListSuccessListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Delivery.Adapter.OrderDeliveryListAdapter
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.EndDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.StartDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.etSearchShop
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_bounced_order.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class BouncedOrderFragment : Fragment(), OrderListSuccessListener, OnEditOrderListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkforOrders(queue!!, token!!, user_id!!, sr_id!!, route_id!!, territory_id!!, StartDate!!, EndDate!!)

        etSearchShop!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
                if (s!!.length == 0) {
                    getAllOrders(Api.get_saved_order+"?user_id="+user_id+/*"&route_id="+route_id+*/"&start_date="+ StartDate+" 00:00:00"+"&end_date="+ EndDate+" 23:59:59"+"&order_status=DELIVERED", queue!!, token!!, mCallback3!!)
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bounced_order, container, false)
        recylerView = view.findViewById(R.id.orderListView)
        progressBar = view.findViewById(R.id.progressBar3)
        return view
    }

    companion object{
        var mCallback3: OrderListSuccessListener? = BouncedOrderFragment()
        var recylerView: RecyclerView? = null
        lateinit var progressBar: ProgressBar
        lateinit var ACTIVITY: OrderDeliveryUpdateActivity
        var token : String? = null
        var user_id : String? = null
        var user_type : String? = null
        var sr_id : String? = null
        var route_id: String? = null
        var territory_id : String? = null
        private var prefs: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null
        var mContext: Context? = null
        var queue: RequestQueue? = null
        var appDatabase: AppDatabase? = null
        private var listener: OnEditOrderListener? = null
        val orderList: ArrayList<OrderList> = ArrayList()
        lateinit var adapter: OrderDeliveryListAdapter
        fun checkforOrders(queue: RequestQueue, token: String, user_id: String, sr_id: String, route_id: String, territory_id: String, start: String, end: String) {
            if (mCallback3!= null) {
                if (sr_id.length == 0){
                    getAllOrders(Api.get_saved_order+"?start_date="+start+" 00:00:00"+"&end_date="+end+" 23:59:59"+"&territory_id="+territory_id+"&order_status=DELIVERED,CANCELLED", queue, token, mCallback3!!)
                }else{
                    getAllOrders(Api.get_saved_order+"?user_id="+user_id+/*"&route_id="+route_id+*/"&start_date="+start+" 00:00:00"+"&end_date="+end+" 23:59:59"+"&order_status=DELIVERED,CANCELLED", queue, token, mCallback3!!)
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
        progressBar.visibility = View.GONE
        orderList.clear()
        try {
            if (array.length() > 0){
                /*no_route_check.visibility = View.GONE
                bodyLayout.visibility = View.VISIBLE*/
                for (i in 0 until array.length()){
                    val orderObj = array.getJSONObject(i)
                    if (orderObj.getString("order_status").equals("DELIVERED", true) || orderObj.getString("order_status").equals("CANCELLED", true)){
                        val brandArray = orderObj.getJSONArray("products")
                        //tvRouteName.setText(orderObj.getString("route_name"))
                        val productItems: ArrayList<Products> = ArrayList()
                        if (brandArray.length() > 0){
                            for (j in 0 until brandArray.length()){
                                val brandObj = brandArray.getJSONObject(j)
                                var bounce = 0
                                if (brandObj.has("bounced_quantity")){
                                    bounce = brandObj.getInt("bounced_quantity")
                                }
                                if (orderObj.getString("order_status").equals("DELIVERED", true) && brandObj.getInt("bounced_quantity") > 0){
                                    if (bounce > 0) {
                                        productItems.add(
                                            Products(
                                                brandObj.getString("product_id"),
                                                brandObj.getString("product_name"),
                                                "",
                                                /*brandObj.getString("brand_id"),
                                                "",*/
                                                brandObj.getDouble("unit_price"),
                                                /*0.0,*/ "",
                                                brandObj.getString("unit_name"),
                                                "", 0, 0,
                                                bounce,
                                                bounce,
                                                brandObj.getDouble("bounced_amount")
                                                /*(brandObj.getInt("bounced_quantity") * brandObj.getDouble("unit_price"))*/
                                            )
                                        )
                                    }
                                }else if (orderObj.getString("order_status").equals("CANCELLED", true)){
                                    if(brandObj.getInt("bounced_quantity") > 0) {
                                        productItems.add(
                                            Products(
                                                brandObj.getString("product_id"),
                                                brandObj.getString("product_name"),
                                                "",
                                                /*brandObj.getString("brand_id"),
                                                "",*/
                                                brandObj.getDouble("unit_price"),
                                                /*0.0,*/ "",
                                                brandObj.getString("unit_name"),
                                                "", 0, 0,
                                                brandObj.getInt("bounced_quantity"),
                                                brandObj.getInt("bounced_quantity"),
                                                brandObj.getDouble("bounced_amount")
                                                /*(brandObj.getInt("bounced_quantity") * brandObj.getDouble("unit_price"))*/
                                            )
                                        )
                                    }
                                }

                            }
                        }
                        if (productItems.size > 0){
                            var grandTotal = 0.0
                            var totalCount = 0
                            for (k in 0 until productItems.size){
                                grandTotal = grandTotal+ productItems[k].ordered_total_price
                                totalCount = totalCount+ productItems[k].bounced_quantity
                            }
                            orderList.add(
                                OrderList(
                                    null,
                                    orderObj.getString("order_no"),
                                    orderObj.getString("ordered_at"),
                                    "CANCELLED",
                                    orderObj.getString("outlet_id"),
                                    orderObj.getString("outlet_name"),
                                    orderObj.getString("route_id"),
                                    orderObj.getString("route_name"),
                                    /*orderObj.getString("distributor_office_code"),*/
                                    /*grandTotal.toString(),
                                    totalCount.toString(),*/
                                    orderObj.getString("total_bounced_amount"),
                                    orderObj.getString("total_bounced_quantity"),
                                    orderObj.getString("latitude"),
                                    orderObj.getString("longitude"),
                                    productItems
                                )
                            )
                        }
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
            orderList.sortByDescending {
                it.orderId
            }

            recylerView.apply {
                if (user_type.equals("TO", true)) {
                    adapter = OrderDeliveryListAdapter(orderList, listener!!, "TO")
                }else{
                    adapter = OrderDeliveryListAdapter(orderList, listener!!, "SO")
                }
                recylerView!!.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }catch (e: Exception){
            e.printStackTrace()
            //Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
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
        user_type = prefs!!.getString(Api.USER_TYPE, "")
        if (user_type.equals("TO", true)){
            sr_id = ""
            route_id = ""
        }else{
            sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
            route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
        }
        territory_id = prefs!!.getString(Api.TERRITORY_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this
        ACTIVITY = context as OrderDeliveryUpdateActivity
        mCallback3 = this

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onEdit(order: OrderList) {

        viewDialog(mContext!!, order)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun viewDialog(mContext: Context, order: OrderList){
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_order_status_update)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        val reasonLayout = dialog.findViewById<LinearLayout>(R.id.reasonLayout)
        val etReason = dialog.findViewById<EditText>(R.id.editTextReason)
        var statusGroup = dialog.findViewById<RadioGroup>(R.id.status_group)
        var radio_group : RadioGroup? = RadioGroup(mContext)
        val itemValue : ArrayList<String> = ArrayList()
        itemValue.add(mContext.resources.getString(R.string.pending))
        itemValue.add(mContext.resources.getString(R.string.delivered))
        itemValue.add(mContext.resources.getString(R.string.bounced))
        radio_group!!.setOrientation(RadioGroup.HORIZONTAL)
        for (i in itemValue.indices) {
            val rbn = RadioButton(mContext)
            rbn.setText(itemValue.get(i))
            rbn.id = i
            rbn.setTextColor(resources.getColor(R.color.text_title))
            rbn.buttonTintList = ColorStateList.valueOf(resources.getColor(R.color.cnl_color_1))
            radio_group.addView(rbn)
        }
        val checkedid = 2
        var isChecked = 0
        statusGroup.addView(radio_group)
        radio_group.check(checkedid)
        radio_group.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            isChecked = checkedId
            if (checkedId == 2){
                reasonLayout.visibility = View.VISIBLE
            }else{
                reasonLayout.visibility = View.GONE
            }
        })

        var dformat = DecimalFormat("#.##")
        outletName.setText(order.outletName)
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
        val orderDate = df.format(oldDate.parse(order.orderedAt))
        tvLastOrderDate.setText(mContext.resources.getString(R.string.last_order_date)+ orderDate)
        var grandTotal = 0.0
        var itemCount = 0
        var brandsStatistics : ArrayList<ProductStatistics> = ArrayList()
        if (order.brands_array.size> 0){
            brandsStatistics.clear()
            for (i in 0 until order.brands_array.size){
                brandsStatistics.add(
                    ProductStatistics(
                        order.brands_array[i].product_id,
                        order.brands_array[i].product_name,
                        order.brands_array[i].unit_name,
                        /*order.brands_array[i].brand_id,*/
                        order.brands_array[i].unit_price,
                        order.brands_array[i].ordered_total_price,
                        order.brands_array[i].ordered_quantity,
                        order.brands_array[i].ordered_quantity,
                        order.brands_array[i].bounced_quantity,
                        order.brands_array[i].ordered_total_price
                    ))
                grandTotal = grandTotal+order.brands_array[i].ordered_total_price
                itemCount = itemCount+order.brands_array[i].ordered_quantity
            }
            val adapter = OutletProductAdapter(brandsStatistics)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal!!.setText(dformat.format(grandTotal).toString())
        tvItemCount!!.setText(itemCount.toString()+mContext.resources.getString(R.string.items))

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        btnSubmit.setOnClickListener {
            ViewUtils.viewDialog(mContext, mContext.resources.getString(R.string.update_order_dialog), object :
                DialogListener {
                override fun onConfirmed() {
                    val status = itemValue.get(isChecked).uppercase(Locale.getDefault())
                    if (order.orderStatus.equals(status, true)){
                        Toast.makeText(mContext, "Order status not changed", Toast.LENGTH_SHORT).show()
                    }else{
                        createOrder(order, status, tvGrandTotal!!.text.toString(), brandsStatistics,  etReason.text.toString(), dialog)
                    }

                }
                override fun onCanceled() {

                }

            })
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    private fun createOrder(order: OrderList, status: String, grandTotal: String,updatedProducts: ArrayList<ProductStatistics>, reason: String, dialog: Dialog){
        if (updatedProducts.size> 0){
            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", order.outletId)
            orderObj.put("order_no", order.orderId)
            orderObj.put("sr_id", sr_id)
            /*orderObj.put("distributor_office_code", order.distOfficeCode)*/
            orderObj.put("grand_total", grandTotal)
            if (reason.trim().length >0){
                orderObj.put("orders_reason", reason)
            }
            orderObj.put("order_status", status)
            val brandsArray = JSONArray()
            for(i in 0 until updatedProducts!!.size){
                val brandObj = JSONObject()
                if (updatedProducts!![i].quantity > 0) {
                    brandObj.put("product_id", updatedProducts[i].product_id)
                    brandObj.put("product", updatedProducts[i].product_name)
                    /*brandObj.put("brand_id", updatedProducts[i].brand_id)*/
                    brandObj.put("quantity", updatedProducts[i].quantity.toString())
                    brandObj.put("bounce", updatedProducts[i].bounced_quantity.toString())
                    brandObj.put("unit_price", updatedProducts[i].unit_price.toString())
                    brandObj.put("total_price", updatedProducts[i].total_price.toString())
                    brandObj.put("unit_name", updatedProducts[i].product_type)
                }
                brandsArray.put(brandObj)
            }
            orderObj.put("products", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            if (obj1.length() >0){
                Log.d("ConfirmOrder", "response: "+obj1)
                submitOrder(obj1, dialog)
            }
        }
    }
    private fun submitOrder(orderObj: JSONObject, dialog: Dialog) {
        ApiServices.apiJSONObjectPOST(Api.update_saved_order, queue!!, token!!, orderObj, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                try {
                    Log.d("ConfirmOrder", "response api: "+response)
                    dialog.dismiss()
                    appDatabase!!.orderListDao().deleteALL()
                    appDatabase!!.saveOrderDao().deleteALL()
                    val message = response.getString("message")
                    ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                        override fun onConfirmed() {
                            checkforOrders(
                                queue!!,
                                token!!,
                                user_id!!,
                                sr_id!!,
                                route_id!!,
                                territory_id!!,
                                StartDate!!,
                                EndDate!!
                            )
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

}