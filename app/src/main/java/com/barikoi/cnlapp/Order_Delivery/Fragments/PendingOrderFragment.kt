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
import com.barikoi.cnlapp.Order_Create.Callback.OnValueChangeListener
import com.barikoi.cnlapp.Order_Create.Callback.OrderListSuccessListener
import com.barikoi.cnlapp.Order_Create.Fragment.ConfirmOrderFragment
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Order_Delivery.Adapter.OrderDeliveryListAdapter
import com.barikoi.cnlapp.Order_Delivery.Adapter.OutletProductDeliveryAdapter
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.EndDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.StartDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.etSearchShop
import com.barikoi.cnlapp.Order_Delivery.RoomDB.UpdateOrder
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_pending_order.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PendingOrderFragment : Fragment(), OrderListSuccessListener, OnEditOrderListener,
    OnValueChangeListener {
    var recylerView: RecyclerView? = null
    var progressBar: ProgressBar? = null
    private var tvItemCount: TextView? = null
    private var tvGrandTotal: TextView? = null
    private var totalItemCount : Int? =  0
    private var grandTotalPrice : Double? = 0.0
    var totalOrderedPrice: String? = null
    var totalOrderedQuantity: String? = null
    lateinit var ACTIVITY: OrderDeliveryUpdateActivity
    var token : String? = null
    var user_id : String? = null
    var user_type : String? = null
    var sr_id : String? = null
    var route_id: String? = null
    var territory_id : String? = null
    var selected_outlet : String? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var appDatabase: AppDatabase? = null
    private var listener: OnEditOrderListener? = null
    var valuelistener: OnValueChangeListener? = null
    val orderList: ArrayList<OrderList> = ArrayList()
    lateinit var adapter: OrderDeliveryListAdapter
    var updatedProducts: ArrayList<ProductStatistics>? = ArrayList()
    var orderedProducts: ArrayList<Products>? = ArrayList()

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
                    getAllOrders(Api.get_saved_order+"?user_id="+user_id+/*"&route_id="+route_id+*/"&start_date="+ StartDate+" 00:00:00"+"&end_date="+ EndDate+" 23:59:59"+"&order_status=PENDING", queue!!, token!!, mCallback!!)
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
        val view = inflater.inflate(R.layout.fragment_pending_order, container, false)
        recylerView = view.findViewById(R.id.orderListView)
        progressBar = view.findViewById(R.id.progressBar1)
        return view
    }


    companion object{
        var mCallback: OrderListSuccessListener? = PendingOrderFragment()
        fun checkforOrders(queue: RequestQueue, token: String, user_id: String, sr_id: String, route_id: String, territory_id: String, start: String, end: String) {
            if (mCallback!= null) {
                if (sr_id.length == 0){
                    getAllOrders(Api.get_saved_order+"?start_date="+start+" 00:00:00"+"&end_date="+end+" 23:59:59"+"&territory_id="+territory_id+"&order_status=PENDING", queue, token, mCallback!!)
                }else{
                    getAllOrders(Api.get_saved_order+"?user_id="+user_id/*+"&route_id="+route_id*/+"&start_date="+start+" 00:00:00"+"&end_date="+end+" 23:59:59"+"&order_status=PENDING", queue, token, mCallback!!)
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
                    Log.d("Order", "exception: "+e.message)
                }

            })
        }
    }

    override fun onSuccess(array: JSONArray) {
        progressBar!!.visibility = View.GONE
        orderList.clear()
        try {
            if (array.length() > 0){
                no_route_check.visibility = View.GONE
                bodyLayout.visibility = View.VISIBLE
                for (i in 0 until array.length()){
                    val orderObj = array.getJSONObject(i)
                    if (orderObj.getString("order_status").equals("PENDING", true)){
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
                                            bounce,
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
        valuelistener = this
        ACTIVITY = context as OrderDeliveryUpdateActivity
        mCallback = this

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onEdit(order: OrderList) {
        appDatabase!!.updateOrderDao().deleteALL()
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
        tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        val statusGroup = dialog.findViewById<RadioGroup>(R.id.status_group)
        val radio_group : RadioGroup? = RadioGroup(mContext)
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
        val checkedid = 0
        var isChecked = 0
        statusGroup.addView(radio_group)
        radio_group.check(checkedid)
        radio_group.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            isChecked = checkedId
        })

        var dformat = DecimalFormat("#.##")
        outletName.setText(order.outletName)
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
        val orderDate = df.format(oldDate.parse(order.orderedAt))
        tvLastOrderDate.setText(mContext.resources.getString(R.string.last_order_date)+ orderDate)

        var grandTotal = 0.0
        var itemCount = 0
        val brandsStatistics : ArrayList<ProductStatistics> = ArrayList()
        if (order.brands_array.size> 0){
            brandsStatistics.clear()
            updatedProducts!!.clear()
            for (i in 0 until order.brands_array.size){
                brandsStatistics.add(
                    ProductStatistics(
                        order.brands_array[i].product_id,
                        order.brands_array[i].product_name,
                        order.brands_array[i].product_code,
                        order.brands_array[i].sku_code,
                        order.brands_array[i].category_code,
                        order.brands_array[i].category_name,
                        order.brands_array[i].category_id,
                        order.brands_array[i].unit_name,
                        order.brands_array[i].unit_id,
                        order.brands_array[i].unit_code,
                        /*order.brands_array[i].brand_id,*/
                        order.brands_array[i].unit_price,
                        order.brands_array[i].discounted_unit_price,
                        order.brands_array[i].ordered_total_price,
                        order.brands_array[i].ordered_quantity,
                        order.brands_array[i].ordered_quantity,
                        0,
                        /*order.brands_array[i].bounced_quantity,*/
                        order.brands_array[i].ordered_total_price
                ))
                grandTotal = grandTotal+order.brands_array[i].ordered_total_price
                itemCount = itemCount+order.brands_array[i].ordered_quantity
            }
            selected_outlet = order.outletId
            totalItemCount = itemCount
            grandTotalPrice = grandTotal
            orderedProducts!!.addAll(order.brands_array)
            updatedProducts!!.addAll(brandsStatistics)
            appDatabase!!.updateOrderDao().insertAll(
                UpdateOrder(
                    null,
                    order.outletId,
                    itemCount,
                    0,
                    grandTotal
                )
            )
            val adapter = OutletProductDeliveryAdapter(brandsStatistics, valuelistener!!, order.outletId, order.orderStatus)
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
                    var status = ""
                    if (isChecked == 2){
                        status = "CANCELLED"
                        //updatedProducts!!.addAll(brandsStatistics)
                        for (i in 0 until updatedProducts!!.size){
                            updatedProducts!![i].bounced_quantity = updatedProducts!![i].quantity
                            updatedProducts!![i].quantity = 0
                        }
                    }else{
                        status = itemValue.get(isChecked).uppercase(Locale.ENGLISH)
                    }

                    if (order.orderStatus.equals(status, true)){
                        Toast.makeText(mContext, "Order status not changed", Toast.LENGTH_SHORT).show()
                    }else{
                        if (updatedProducts!!.size >0) {
                            createOrder(
                                order,
                                status,
                                tvGrandTotal!!.text.toString(),
                                updatedProducts!!,
                                dialog
                            )
                        }else{
                            createOrder(
                                order,
                                status,
                                tvGrandTotal!!.text.toString(),
                                brandsStatistics,
                                dialog
                            )
                        }
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

    private fun createOrder(order: OrderList, status: String, grandTotal: String,updatedProducts: ArrayList<ProductStatistics>, dialog: Dialog){
        if (updatedProducts.size> 0){
            var deliveredQuantity = 0
            var deliveredAmount = 0.0
            var bouncedQuantity = 0
            var bouncedAmount = 0.0
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val cal = Calendar.getInstance()
            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DATE, 1)
            val nextDay = df.format(cal.time)

            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", order.outletId)
            orderObj.put("order_no", order.orderId)
            orderObj.put("user_id", user_id)
            orderObj.put("employee_id", sr_id)
            /*orderObj.put("ordered_at", today)*/
            orderObj.put("delivered_at", today)
            /*orderObj.put("distributor_office_code", order.distOfficeCode)*/
            orderObj.put("total_ordered_amount", order.grandTotal)
            orderObj.put("total_ordered_quantity", order.totalQuantity)
            orderObj.put("order_status", status)
            val brandsArray = JSONArray()
            for(i in 0 until updatedProducts!!.size){
                val brandObj = JSONObject()
                if (updatedProducts[i].quantity > 0 ) {
                    val bounceAmount = updatedProducts[i].bounced_quantity * updatedProducts[i].discounted_unit_price
                    brandObj.put("product_id", updatedProducts[i].product_id)
                    /*brandObj.put("brand_id", addedProducts!![j].brand_id)*/
                    brandObj.put("product_code", updatedProducts[i].product_code)
                    brandObj.put("product_name", updatedProducts[i].product_name)
                    brandObj.put("brand_id", null)
                    brandObj.put("sku_code", updatedProducts[i].sku_code)
                    brandObj.put("unit_id", updatedProducts[i].unit_id)
                    brandObj.put("unit_name", updatedProducts[i].unit_name)
                    brandObj.put("unit_code", updatedProducts[i].unit_code)
                    brandObj.put("unit_price", updatedProducts[i].unit_price.toString())
                    brandObj.put("discounted_unit_price", updatedProducts[i].discounted_unit_price.toString())
                    brandObj.put("category_id", updatedProducts[i].category_id)
                    brandObj.put("category_name", updatedProducts[i].category_name)
                    brandObj.put("category_code", updatedProducts[i].category_code)
                    brandObj.put("ordered_quantity", updatedProducts[i].ordered_quantity.toString())
                    brandObj.put("delivered_quantity", updatedProducts[i].quantity.toString())
                    brandObj.put("bounced_quantity", updatedProducts[i].bounced_quantity.toString())
                    brandObj.put("ordered_amount", updatedProducts[i].ordered_price.toString())
                    brandObj.put("delivered_amount", updatedProducts[i].total_price.toString())
                    brandObj.put("bounced_amount", bounceAmount.toString())
                    brandsArray.put(brandObj)
                    deliveredQuantity = deliveredQuantity + updatedProducts[i].quantity
                    deliveredAmount = deliveredAmount + updatedProducts[i].total_price
                    bouncedQuantity = bouncedQuantity + updatedProducts[i].bounced_quantity
                    bouncedAmount = bounceAmount + bounceAmount
                }
                else if(updatedProducts[i].quantity == 0 && updatedProducts[i].bounced_quantity > 0){
                    val bounceAmount = updatedProducts[i].bounced_quantity * updatedProducts[i].unit_price
                    brandObj.put("product_id", updatedProducts[i].product_id)
                    /*brandObj.put("brand_id", addedProducts!![j].brand_id)*/
                    brandObj.put("product_code", updatedProducts[i].product_code)
                    brandObj.put("product_name", updatedProducts[i].product_name)
                    brandObj.put("brand_id", null)
                    brandObj.put("sku_code", updatedProducts[i].sku_code)
                    brandObj.put("unit_id", updatedProducts[i].unit_id)
                    brandObj.put("unit_name", updatedProducts[i].unit_name)
                    brandObj.put("unit_code", updatedProducts[i].unit_code)
                    brandObj.put("unit_price", updatedProducts[i].unit_price.toString())
                    brandObj.put("discounted_unit_price", updatedProducts[i].discounted_unit_price.toString())
                    brandObj.put("category_id", updatedProducts[i].category_id)
                    brandObj.put("category_name", updatedProducts[i].category_name)
                    brandObj.put("category_code", updatedProducts[i].category_code)
                    brandObj.put("ordered_quantity", updatedProducts[i].ordered_quantity.toString())
                    brandObj.put("delivered_quantity", updatedProducts[i].quantity.toString())
                    brandObj.put("bounced_quantity", updatedProducts[i].bounced_quantity.toString())
                    brandObj.put("ordered_amount", updatedProducts[i].ordered_price.toString())
                    brandObj.put("delivered_amount", updatedProducts[i].total_price.toString())
                    brandObj.put("bounced_amount", bounceAmount.toString())
                    brandsArray.put(brandObj)
                    deliveredQuantity = deliveredQuantity + updatedProducts[i].quantity
                    deliveredQuantity = deliveredQuantity + updatedProducts[i].quantity
                    deliveredAmount = deliveredAmount + updatedProducts[i].total_price
                    bouncedQuantity = bouncedQuantity + updatedProducts[i].bounced_quantity
                    bouncedAmount = bounceAmount + bounceAmount
                }

            }
            orderObj.put("total_delivered_amount", deliveredAmount)
            orderObj.put("total_delivered_quantity", deliveredQuantity)
            orderObj.put("total_bounced_amount", bouncedAmount)
            orderObj.put("total_bounced_quantity", bouncedQuantity)
            orderObj.put("products", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            if (obj1.length() >0){
                if (brandsArray.length() > 0) {
                    Log.d("ConfirmOrder", "response: " + obj1)
                    if (status.equals("DELIVERED", true)){
                    if (deliveredQuantity > 0) {
                        submitOrder(obj1, dialog)
                    }else{
                        ViewUtils.viewDialogResponse(mContext!!, "Kindly change the status to BOUNCED option to continue", object : DialogListener{
                            override fun onConfirmed() {

                            }

                            override fun onCanceled() {

                            }

                        })
                    }
                    }else{
                        submitOrder(obj1, dialog)
                    }
                }else{
                    Toast.makeText(mContext, "No products on this order", Toast.LENGTH_SHORT).show()
                }
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
                            checkforOrders(queue!!, token!!, user_id!!, sr_id!!, route_id!!, territory_id!!, StartDate!!, EndDate!!)
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

    override fun onValueChanged(products: Any, position: Int) {
        var dformat = DecimalFormat("#.##")
        products as ProductStatistics
        val prodList = appDatabase!!.updateOrderDao().getOrdersDB(selected_outlet!!)
        val itemCount = prodList!![0].itemsCount
        val grandTotal = prodList[0].totalPrice
        if (itemCount == 1 || itemCount == 0){
            tvItemCount!!.setText(itemCount.toString()+"Item")
        }else{
            tvItemCount!!.setText(itemCount.toString()+"Items")
        }
        try{
            Log.d("Product", "addedProducts size: "+updatedProducts!!.size)
            val exists = updatedProducts?.find {
                it.product_id == products.product_id
            }
            Log.d("Product", "addedProducts size: "+exists)
            if (exists != null){
                updatedProducts!!.remove(exists)
                updatedProducts!!.add(
                    ProductStatistics(
                        products.product_id, products.product_name,
                        products.product_code, products.sku_code,
                        products.category_code, products.category_name,
                        products.category_id, products.unit_name,
                        products.unit_id, products.unit_code,
                        products.unit_price, products.discounted_unit_price,
                        products.total_price,
                        products.quantity, products.quantity,
                        products.bounced_quantity, products.total_price)
                )

            }else{
                updatedProducts!!.add(
                    ProductStatistics(
                        products.product_id, products.product_name,
                        products.product_code, products.sku_code,
                        products.category_code, products.category_name,
                        products.category_id, products.unit_name,
                        products.unit_id, products.unit_code,
                        products.unit_price, products.discounted_unit_price,
                        products.total_price,
                        products.quantity, products.quantity,
                        products.bounced_quantity, products.total_price
                    )
                )
            }
        }catch (e: Exception){
            Log.d("Product", "exception 2: "+e.message+" "+position)
            e.printStackTrace()
        }
        tvGrandTotal!!.setText(dformat.format(grandTotal).toString())
        //totalAmount = dformat.format(grandTotal).toString()
        //grandTotalPrice = dformat.format(grandTotal).toDouble()

    }
}