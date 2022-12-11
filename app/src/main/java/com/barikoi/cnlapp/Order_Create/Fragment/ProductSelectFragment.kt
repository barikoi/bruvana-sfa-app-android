package com.barikoi.cnlapp.Order_Create.Fragment

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Fragment.CreateAttendanceFragment
import com.barikoi.cnlapp.Order_Create.Adapter.ProductListAdapter
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnValueChangeListener
import com.barikoi.cnlapp.Order_Create.RoomDB.SaveOrder
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_product_select.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ProductSelectFragment : Fragment(), OnValueChangeListener {

    var recylerView: RecyclerView? = null
    var shopTitle: TextView? = null
    var sortTitle: TextView? = null
    private var totalItemCount: TextView? = null
    var tvgrandTotal: TextView? = null
    var saveOrder: AppCompatButton? = null
    var noOrder: AppCompatButton? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var token : String? = null
    var user_id : String? = null
    var sr_id : String? = null
    var selectedShop : Shops? =  null
    var selectedOrder : OrderList? =  null
    var totalAmount : String? =  null
    var grandTotalPrice : Double? = 0.0
    var shopName : String? =  null
    var shopId : String? =  null
    var listener: OnValueChangeListener? = null
    var et_search: AutoCompleteTextView? = null
    private var adapter: ProductListAdapter? = null
    var productsList: ArrayList<Products>? = ArrayList()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var loading: ProgressBar? = null
    lateinit var ACTIVITY: MainActivity
    private var appDatabase: AppDatabase? = null
    private var addedProducts: ArrayList<Products>? = ArrayList()
    /*var itemCount = 0
    var grandTotal = 0.0*/
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = this.arguments

        if (bundle != null) {
            if (bundle.containsKey("from")){
                if (bundle.getString("from").equals("Shop")){
                    selectedShop = bundle.getSerializable("Shop") as Shops?
                    shopName = selectedShop!!.shop_name
                    shopId = selectedShop!!.shop_id
                    addedProducts!!.clear()
                }else if (bundle.getString("from").equals("Order")){
                    selectedOrder = bundle.getSerializable("Order") as OrderList?
                    shopName = selectedOrder!!.outletName
                    addedProducts!!.clear()
                    appDatabase!!.saveOrderDao().deleteALL()
                    var itemCountt = 0
                    for (i in 0 until selectedOrder!!.brands_array.size){
                        itemCountt = itemCountt+selectedOrder!!.brands_array[i].ordered_quantity
                    }

                    if (itemCountt == 1 || itemCountt == 0){
                        totalItemCount!!.setText(itemCountt.toString()+"Item")
                    }else{
                        totalItemCount!!.setText(itemCountt.toString()+"Items")
                    }
                    tvgrandTotal!!.setText("Total "+selectedOrder!!.grandTotal)
                    appDatabase!!.saveOrderDao().insertAll(
                        SaveOrder(
                            null,
                            selectedOrder!!.outletId,
                            itemCountt,
                            selectedOrder!!.grandTotal.toDouble()
                        )
                    )
                }
            }

        }

        shopTitle!!.text = shopName
        val gd = GradientDrawable()
        gd.setColor(mContext!!.resources.getColor(R.color.white))
        gd.cornerRadius = 5f
        gd.setStroke(2, mContext!!.resources.getColor(R.color.cnl_color_2))
        previous_order.setBackgroundDrawable(gd)

        previous_order.setOnClickListener {
            try {
                ApiServices.apiGET(
                    Api.previous_order + "?sr_id=" + sr_id + "&route_id=" + selectedShop!!.route_code + "&outlet_id=" + shopId,
                    queue!!,
                    token!!,
                    object : ApiServiceListener {
                        override fun onResponseSuccess(response: String) {
                            getPreviousOrders(response)
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
                            Toast.makeText(
                                mContext!!.applicationContext,
                                e.message, Toast.LENGTH_SHORT
                            ).show()
                        }

                    })
            }catch (e: Exception){
                e.printStackTrace()
                Sentry.captureException(e)
            }
        }

        if (selectedOrder != null){
            no_order.visibility = View.GONE
            save_order.visibility = View.GONE
            update_order.visibility = View.VISIBLE
        }else{
            no_order.visibility = View.VISIBLE
            save_order.visibility = View.VISIBLE
            update_order.visibility = View.GONE
        }

        update_order.setOnClickListener {
            appDatabase!!.saveOrderDao().deleteALL()
            val builder = SpannableStringBuilder()
            val str1= SpannableString(shopName)
            str1.setSpan(ForegroundColorSpan(resources.getColor(R.color.cnl_color_1)), 0, str1.length, 0)
            builder.append(str1)
            ViewUtils.viewDialog(mContext!!, "Are you sure want to update "+str1+"'s order?", object :
                DialogListener {
                override fun onConfirmed() {
                    progressBar.visibility = View.VISIBLE
                    getLocation("update_order")
                }
                override fun onCanceled() {

                }
            })

        }
        getAllProducts()
    }

    private fun getPreviousOrders(response: String) {
        var lastDeliveryDate = ""
        var productItems: ArrayList<ProductStatistics> = ArrayList()
        val obj = JSONObject(response)
        val outletssArray = obj.getJSONArray("previous_orders")
        if (outletssArray.length() > 0){
            for (i in 0 until outletssArray.length()){
                productItems.clear()
                val outletObj = outletssArray.getJSONObject(i)
                val brandArray = outletObj.getJSONArray("brands")
                //val outletName = outletObj.getString("outlet_name")
                lastDeliveryDate = outletObj.getString("order_delivery_date")
                if (brandArray.length() >0){
                    for (j in 0 until brandArray.length()){
                        val brandObj = brandArray.getJSONObject(j)
                        productItems.add(
                            ProductStatistics(
                                brandObj.getString("product_id"),
                                brandObj.getString("product"),
                                brandObj.getString("unit_name"),
                                brandObj.getString("brand_id"),
                                brandObj.getDouble("unit_price"),
                                brandObj.getInt("quantity"),
                                brandObj.getInt("bounce"),
                                brandObj.getDouble("total_price")
                            )
                        )
                    }
                }

            }

        }
        viewDialog(mContext!!, shopName!!, lastDeliveryDate, productItems)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product_select, container, false)

        shopTitle = view.findViewById(R.id.selectedShop)
        et_search = view.findViewById(R.id.editTextSearchProduct)
        recylerView = view.findViewById(R.id.productlist)
        totalItemCount = view.findViewById(R.id.totalItemCount)
        tvgrandTotal = view.findViewById(R.id.totalAmount)
        saveOrder = view.findViewById(R.id.save_order)
        noOrder = view.findViewById(R.id.no_order)
        loading = view.findViewById(R.id.progressBar)
        sortTitle = view.findViewById(R.id.sortTitle)

        sortTitle!!.setOnClickListener {
            val popup = PopupMenu(mContext, sortTitle)
            popup.menuInflater.inflate(R.menu.sort_menu_product, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.menu_ztoa->{
                            productsList!!.sortByDescending {
                                it.product_name
                            }
                            if (productsList!!.size > 0){
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

                            sortTitle!!.setText(resources.getString(R.string.ztoa))
                        }
                        R.id.menu_atoz->{
                            productsList!!.sortBy {
                                it.product_name
                            }
                            if (productsList!!.size > 0){
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                            sortTitle!!.setText(resources.getString(R.string.atoz))
                        }
                        R.id.menu_mostfrequent->{
                            productsList!!.sortByDescending {
                                    it.quantity_last_month
                            }
                            if (productsList!!.size > 0){
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }

                            sortTitle!!.setText(resources.getString(R.string.most_frequent))
                        }
                        R.id.menu_lowstock->{
                            productsList!!.sortBy {
                                it.stock_available
                            }
                            if (productsList!!.size > 0){
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                            sortTitle!!.setText(resources.getString(R.string.low_stock))
                        }
                    }
                    return true
                }

            })
            popup.show()
        }

        //adapter = ProductListAdapter(ArrayList(), listener!!)



        et_search!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0){
                    getAllProducts()
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })

        saveOrder!!.setOnClickListener {
            if (addedProducts!!.size >0) {
                appDatabase!!.saveOrderDao().deleteALL()
                val builder = SpannableStringBuilder()
                val str1 = SpannableString(shopName)
                str1.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.cnl_color_1)),
                    0,
                    str1.length,
                    0
                )
                builder.append(str1)
                ViewUtils.viewDialog(
                    mContext!!,
                    "Are you sure want to save " + str1 + "'s order?",
                    object :
                        DialogListener {
                        override fun onConfirmed() {
                            progressBar.visibility = View.VISIBLE
                            getLocation("save_order")
                        }

                        override fun onCanceled() {

                        }

                    })
            }else{
                ViewUtils.viewDialogResponse(
                    mContext!!,
                    "No products selected to order",
                    object :
                        DialogListener {
                        override fun onConfirmed() {

                        }

                        override fun onCanceled() {

                        }

                    })
            }
        }

        noOrder!!.setOnClickListener {
            appDatabase!!.saveOrderDao().deleteALL()
            ViewUtils.viewDialog(mContext!!, "Are you sure want to select no order?", object :
                DialogListener {
                override fun onConfirmed() {
                    progressBar.visibility = View.VISIBLE
                    getLocation("no_order")
                }
                override fun onCanceled() {

                }
            })

        }

        return view
    }

    fun getLocation(choice: String){
        val lm = mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext!!)
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationCallback = object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (!location.latitude.isNaN()) {
                        if (!location.isFromMockProvider) {
                            if (choice.equals("no_order")){
                                submitNoOrder(location)
                            }else if(choice.equals("update_order")){
                                updateOrder(location)
                            }else{
                                submitOrder(location)
                            }

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
            ViewUtils.showGPSDisabledAlertToUser(mContext!!)
        }
    }

    private fun updateOrder(location: Location) {
        if (addedProducts!!.size >0){
            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", shopId)
            orderObj.put("sr_id", sr_id)
            //orderObj.put("ordered_at", "2022-10-25 09:22:00")
            orderObj.put("order_no", selectedOrder!!.orderId)
            orderObj.put("distributor_office_code", selectedOrder!!.distOfficeCode)
            orderObj.put("grand_total", grandTotalPrice.toString())
            orderObj.put("longitude", location.longitude.toString())
            orderObj.put("latitude", location.latitude.toString())
            orderObj.put("orders_status", "SAVED")
            val brandsArray = JSONArray()
            //val brandList = orderList[i].brands_array
            for (j in 0 until addedProducts!!.size){
                val brandObj = JSONObject()
                if (addedProducts!![j].ordered_quantity > 0) {
                    brandObj.put("product_id", addedProducts!![j].product_id)
                    brandObj.put("product", addedProducts!![j].product_name)
                    brandObj.put("brand_id", addedProducts!![j].brand_id)
                    brandObj.put("quantity", addedProducts!![j].ordered_quantity.toString())
                    brandObj.put("bounce", "0")
                    brandObj.put("unit_name", addedProducts!![j].unit_name)
                    brandObj.put("unit_price", addedProducts!![j].unit_price.toString())
                    brandObj.put("total_price", addedProducts!![j].ordered_total_price.toString())
                    brandsArray.put(brandObj)
                }

            }

            orderObj.put("brands", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            if (obj1.length() >0){
                Log.d("ConfirmOrder", "response: "+obj1)
                ApiServices.apiJSONObjectPOST(Api.update_saved_order, queue!!, token!!, obj1, object : ApiServiceListener{
                    override fun onResponseSuccess(response: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onJSONResponseSuccess(response: JSONObject) {
                        progressBar.visibility = View.GONE
                        val message = response.getString("message")
                        //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()

                        ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                            override fun onConfirmed() {
                                CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
                            }

                            override fun onCanceled() {
                                TODO("Not yet implemented")
                            }

                        })
                    }

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {

                    }

                    override fun onResponseFailure(error: VolleyError) {
                        progressBar.visibility = View.GONE
                        ViewUtils.getErrorResponse(error, mContext!!)
                    }
                    override fun onException(e: Exception) {
                        progressBar.visibility = View.GONE
                    }

                })
            }
        }
    }

    private fun submitOrder(location: Location) {
        if (addedProducts!!.size >0){
        val obj1 = JSONObject()
        val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", shopId)
            orderObj.put("sr_id", sr_id)
            //orderObj.put("ordered_at", "2022-10-25 09:22:00")
            orderObj.put("distributor_office_code", selectedShop!!.distributor_office_code)
            orderObj.put("grand_total", grandTotalPrice.toString())
            orderObj.put("longitude", location.longitude.toString())
            orderObj.put("latitude", location.latitude.toString())
            //orderObj.put("orders_status", "SAVED")
            val brandsArray = JSONArray()
            //val brandList = orderList[i].brands_array
            for (j in 0 until addedProducts!!.size){
                val brandObj = JSONObject()
                if (addedProducts!![j].ordered_quantity >0) {
                    brandObj.put("product_id", addedProducts!![j].product_id)
                    brandObj.put("product", addedProducts!![j].product_name)
                    brandObj.put("brand_id", addedProducts!![j].brand_id)
                    brandObj.put("quantity", addedProducts!![j].ordered_quantity.toString())
                    brandObj.put("bounce", "0")
                    brandObj.put("unit_name", addedProducts!![j].unit_name)
                    brandObj.put("unit_price", addedProducts!![j].unit_price.toString())
                    brandObj.put("total_price", addedProducts!![j].ordered_total_price.toString())
                }
                brandsArray.put(brandObj)
            }

            orderObj.put("brands", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

        if (obj1.length() >0){
            Log.d("ConfirmOrder", "response: "+obj1)
            ApiServices.apiJSONObjectPOST(Api.confirm_order, queue!!, token!!, obj1, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    TODO("Not yet implemented")
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    val message = response.getString("message")
                    progressBar.visibility = View.GONE
                    //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    appDatabase!!.saveOrderDao().deleteByShop(shopId!!)
                    appDatabase!!.orderListDao().deleteByShop(shopId!!)

                    ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                        override fun onConfirmed() {
                            CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
                        }

                        override fun onCanceled() {
                            TODO("Not yet implemented")
                        }

                    })
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {

                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, mContext!!)
                    progressBar.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    progressBar.visibility = View.GONE
                }

            })
        }
        }
    }

    private fun submitNoOrder(location: Location) {
        val obj1 = JSONObject()
        val ordersArray = JSONArray()
        val orderObj = JSONObject()
        orderObj.put("outlet_id", shopId)
        orderObj.put("sr_id", sr_id)
        orderObj.put("longitude", location.longitude.toString())
        orderObj.put("latitude", location.latitude.toString())
        ordersArray.put(orderObj)
        obj1.put("orders", ordersArray)

        if (obj1.length() >0){
            Log.d("ConfirmOrder", "response: "+obj1)
            ApiServices.apiJSONObjectPOST(Api.no_order, queue!!, token!!, obj1, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    TODO("Not yet implemented")
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    val message = response.getString("message")
                    //Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE

                    ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener{
                        override fun onConfirmed() {
                            editor!!.putString(Api.ORDERED_ROUTE_ID, selectedShop!!.route_code)
                            editor!!.commit()
                            CreateOrderFragment.setCurrentFragment(ShopSelectFragment(), ACTIVITY)
                        }

                        override fun onCanceled() {
                            TODO("Not yet implemented")
                        }

                    })
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {

                }

                override fun onResponseFailure(error: VolleyError) {
                    progressBar.visibility = View.GONE
                    ViewUtils.getErrorResponse(error, mContext!!)
                }

                override fun onException(e: Exception) {
                    progressBar.visibility = View.GONE
                }

            })
        }
    }

    private fun getAllProducts() {
        loading!!.visibility = View.VISIBLE
        val request = StringRequest(
            Request.Method.GET, Api.all_product_list+"?with_stock=1&sr_id=" + sr_id + "&route_id=" + selectedShop!!.route_code,
            {
                    response ->
                try {
                    loading!!.visibility = View.GONE

                    val data = JSONObject(response)
                    if (data.has("products") && !data.isNull("products")){
                        val productArray = data.getJSONArray("products")
                        if (productArray.length() > 0){
                            productsList!!.clear()
                            for (i in 0 until productArray.length()) {
                                var orderedQty = 0
                                var orderedTotalPrice = 0.0
                                val productObj = productArray.getJSONObject(i)
                                val productId = productObj.getString("id")
                                val productName = if (!productObj.isNull("product_name")) productObj.getString("product_name") else ""
                                val productCode = if (!productObj.isNull("product_code")) productObj.getString("product_code") else ""
                                val brandId = if (!productObj.isNull("brand_id")) productObj.getString("brand_id") else ""
                                val brandName = if (!productObj.isNull("brand__name")) productObj.getString("brand__name") else ""
                                //val price = if (!productObj.isNull("price")) productObj.getDouble("price") else 0.0
                                val discount = if (!productObj.isNull("discount")) productObj.getDouble("discount") else 0.0
                                val image = if (!productObj.isNull("image")) productObj.getString("image") else ""
                                val unitName = if (!productObj.isNull("unit_name")) productObj.getString("unit_name") else ""
                                val categoryName = if (!productObj.isNull("category_name")) productObj.getString("category_name") else ""
                                val qtyLastMonth = if (!productObj.isNull("quantity_last_month")) productObj.getInt("quantity_last_month") else 0
                                val availableStock = if (!productObj.isNull("current_available_stock")) productObj.getInt("current_available_stock") else 0
                                var price = 0.0
                                if (!productObj.isNull("price")) {
                                    price = productObj.getDouble("price")
                                } else {
                                    price = 0.0
                                }
                                if (selectedOrder != null){
                                    //var orderlistDB = appDatabase!!.orderListDao().getOrdersDB(selectedOrder!!.outletId.toString())
                                    val exist = selectedOrder!!.brands_array.find {
                                        it.product_id == productId
                                    }

                                    if (exist != null){
                                        orderedQty = exist.ordered_quantity
                                        orderedTotalPrice = exist.ordered_total_price
                                    }
                                }
                                val products = Products(
                                    productId,
                                    productName, productCode, brandId, brandName, price, discount, image, unitName, categoryName,
                                    qtyLastMonth, availableStock, 0, orderedQty, orderedTotalPrice)

                                productsList!!.add(products)
                                if (orderedQty > 0){
                                    addedProducts!!.add(products)
                                }
                            }

                            if (productsList!!.size > 0){
                                productsList!!.sortBy { it.product_name }
                                sortTitle!!.setText(resources.getString(R.string.atoz))
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                        }

                    }
                }catch (e:Exception){
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
                    Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue!!.add(request)
    }
    fun viewDialog(mContext: Context, outlet_name: String, lastOrder: String, listItem: ArrayList<ProductStatistics>){
        val dialog = Dialog(mContext)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.popup_product_list)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        var dformat = DecimalFormat("#.##")
        outletName.setText(outlet_name)
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yy", Locale.ENGLISH)
        if (lastOrder.length > 0) {
            val orderDate = df.format(oldDate.parse(lastOrder))
            tvLastOrderDate.setText(mContext.resources.getString(R.string.last_order_date) + orderDate)
        }
        tvItemCount.setText(listItem.size.toString()+mContext.resources.getString(R.string.items))

        var grandTotal = 0.0
        if (listItem.size> 0){
            for (i in 0 until listItem.size){
                grandTotal = grandTotal+listItem[i].total_price
            }

            val adapter = OutletProductAdapter(listItem)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal.setText(dformat.format(grandTotal).toString())

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
    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this

        ACTIVITY = context as MainActivity
    }

    override fun onValueChanged(products: Any, position: Int) {
        var dformat = DecimalFormat("#.##")
        products as Products
        var itemCount = 0
        var grandTotal = 0.0
        val prodList = appDatabase!!.saveOrderDao().getOrdersDB(prefs!!.getString(Api.SELECTED_SHOP_ID, "")!!)
        itemCount = prodList!![0].itemsCount
        grandTotal = prodList[0].totalPrice
        if (itemCount == 1 || itemCount == 0){
            totalItemCount!!.setText(itemCount.toString()+"Item")
        }else{
            totalItemCount!!.setText(itemCount.toString()+"Items")
        }
        try{
            Log.d("Product", "addedProducts size: "+addedProducts!!.size)
            //val exists = products.product_id in arrayOf(addedProducts)
            val exists = addedProducts?.find {
                it.product_id == products.product_id
            }
            Log.d("Product", "addedProducts size: "+exists)
            if (exists != null){
                addedProducts!!.remove(exists)
                addedProducts!!.add(
                    Products(
                    products.product_id, products.product_name,
                    products.product_code, products.brand_id,
                    products.brand_name, products.unit_price,
                    products.discount, products.imageUrl,
                    products.unit_name, products.category_name,
                    products.quantity_last_month,
                    products.stock_available, products.bounced_quantity, products.ordered_quantity, products.ordered_total_price
                )
                )

            }else{
                addedProducts!!.add(
                    Products(
                    products.product_id, products.product_name,
                    products.product_code, products.brand_id,
                    products.brand_name, products.unit_price,
                    products.discount, products.imageUrl,
                    products.unit_name, products.category_name,
                    products.quantity_last_month,
                    products.stock_available, products.bounced_quantity, products.ordered_quantity, products.ordered_total_price
                )
                )
            }
        }catch (e: Exception){
            Log.d("Product", "exception 2: "+e.message+" "+position)
            e.printStackTrace()
        }
        tvgrandTotal!!.setText("Total "+dformat.format(grandTotal).toString())
        totalAmount = dformat.format(grandTotal).toString()
        grandTotalPrice = dformat.format(grandTotal).toDouble()

    }

}