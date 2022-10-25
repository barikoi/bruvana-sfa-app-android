package com.barikoi.cnlapp.Fragment.so_view

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.so_view.ProductListAdapter
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.RoomDb.OrderList
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.barikoi.cnlapp.callback.DialogListener
import com.barikoi.cnlapp.callback.OnValueChangeListener
import com.google.android.gms.location.*
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat


class ProductSelectFragment : Fragment(), OnValueChangeListener {

    var recylerView: RecyclerView? = null
    var shopTitle: TextView? = null
    var totalItemCount: TextView? = null
    var tvgrandTotal: TextView? = null
    var saveOrder: TextView? = null
    var totalAmount : String? =  null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var user_id : String? = null
    var selectedShop : Shops? =  null
    var shopName : String? =  null
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

        val bundle = this.arguments

        if (bundle != null) {
            selectedShop = bundle.getSerializable(Api.SELECTED_SHOP) as Shops?
            shopName = selectedShop!!.shop_name
        }

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
        saveOrder = view.findViewById(R.id.saveOrder)
        loading = view.findViewById(R.id.progressBar)

        //adapter = ProductListAdapter(ArrayList(), listener!!)

        shopTitle!!.text = shopName

        getAllProducts()

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
            ViewUtils.viewDialog(mContext!!, "Are you sure want to save "+shopName+"'s order?", object : DialogListener{
                override fun onConfirmed() {
                    getLocation()
                }
                override fun onCanceled() {

                }

            })
        }

        return view
    }

    fun saveOrderDB(location: Location){
        val orderListDB = appDatabase!!.orderListDao().getOrdersDB(selectedShop!!.shop_id)
        if (orderListDB!!.size > 0){
            appDatabase!!.orderListDao().deleteByShop(selectedShop!!.shop_id)
            appDatabase!!.orderListDao().insertAll(OrderList(null,
                selectedShop!!.shop_id.toInt(),
                selectedShop!!.shop_name,
                selectedShop!!.route_code,
                selectedShop!!.distributor_office_code, totalAmount!!,
                location.latitude.toString(), location.longitude.toString(), addedProducts!!))
        }else{
            appDatabase!!.orderListDao().insertAll(OrderList(null,
                selectedShop!!.shop_id.toInt(),
                selectedShop!!.shop_name,
                selectedShop!!.route_code,
                selectedShop!!.distributor_office_code, totalAmount!!,
                latitude.toString(), longitude.toString(), addedProducts!!))
        }

        CreateOrderFragment.setCurrentFragment(ConfirmOrderFragment(), ACTIVITY)
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
                    if (!location.latitude.isNaN()) {
                        if (!location.isFromMockProvider) {
                            saveOrderDB(location)
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
    private fun getAllProducts() {
        loading!!.visibility = View.VISIBLE
        val request = StringRequest(
            Request.Method.GET, Api.all_product_list+"?with_quantity_last_month=1&with_available_stock=1",
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
                                val productObj = productArray.getJSONObject(i)

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
                                val products = Products(
                                    productObj.getString("id"),
                                    productName, productCode, brandId, brandName, price, discount, image, unitName, categoryName,
                                    qtyLastMonth, availableStock, 0, 0.0)

                                productsList!!.add(products)
                            }

                            if (productsList!!.size > 0){
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
        queue!!.add(request)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        //token = prefs.getString("token", "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        mContext = context
        listener = this

        addedProducts!!.clear()

        ACTIVITY = context as MainActivity
    }

    override fun onValueChanged(products: Products, position: Int) {
        var dformat = DecimalFormat("#.##")
        Log.d("Product", "item: "+recylerView!!.adapter!!.itemCount)
        /*for (i in 0 until recylerView!!.adapter!!.itemCount) {

        }*/
        var itemCount = 0
        var grandTotal = 0.0
        val viewItem: View = recylerView!!.getChildAt(position)
        val etCount = viewItem.findViewById<View>(R.id.tvCount) as EditText
        //val tvUnitPrice = viewItem.findViewById<View>(R.id.tvPerUnit) as TextView
        //itemCount = itemCount + etCount.text.toString().toInt()

        val tvSubTotal = viewItem.findViewById<TextView>(R.id.tvTotalPrice) as TextView
        //grandTotal = grandTotal + tvUnitPrice.text.toString().toDouble()
        try {

            for (i in 0 until recylerView!!.adapter!!.itemCount) {
                Log.d("Product", "all Item: "+adapter!!.itemCount)
                Log.d("Product", "view Item: "+i)
                val viewItem1: View = recylerView!!.getChildAt(i)
                val etCount1 = viewItem1.findViewById<View>(R.id.tvCount) as EditText
                val tvUnitPrice1 = viewItem1.findViewById<View>(R.id.tvPerUnit) as TextView
                itemCount = itemCount + etCount1.text.toString().toInt()

                val tvSubTotal1 = viewItem1.findViewById<TextView>(R.id.tvTotalPrice) as TextView
                grandTotal = grandTotal + tvSubTotal1.text.toString().toDouble()
            }



        }catch (e:Exception){
            Log.d("Product", "exception: "+e.message+" "+position)
            e.printStackTrace()
        }
        if (addedProducts!!.size > 0){
            for (j in 0 until addedProducts!!.size){
                if (addedProducts!![j].product_id.equals(products.product_id)){
                    addedProducts!!.removeAt(j)
                    /*addedProducts!!.add(Products(
                        products.product_id, products.product_name,
                        products.product_code, products.brand_id,
                        products.brand_name, products.unit_price,
                        products.discount, products.imageUrl,
                        products.unit_name, products.category_name,
                        products.quantity_last_month,
                        products.stock_available, etCount.text.toString().toInt(), dformat.format(tvSubTotal.text.toString().toDouble()).toDouble()
                    ))*/
                }else{
                    addedProducts!!.add(Products(
                        products.product_id, products.product_name,
                        products.product_code, products.brand_id,
                        products.brand_name, products.unit_price,
                        products.discount, products.imageUrl,
                        products.unit_name, products.category_name,
                        products.quantity_last_month,
                        products.stock_available, etCount.text.toString().toInt(), dformat.format(tvSubTotal.text.toString().toDouble()).toDouble()
                    ))
                }
            }
        }else{
            addedProducts!!.add(Products(
                products.product_id, products.product_name,
                products.product_code, products.brand_id,
                products.brand_name, products.unit_price,
                products.discount, products.imageUrl,
                products.unit_name, products.category_name,
                products.quantity_last_month,
                products.stock_available, etCount.text.toString().toInt(), dformat.format(grandTotal).toDouble()
            ))
        }
        totalItemCount!!.setText(itemCount.toString()+"Items")
        tvgrandTotal!!.setText("Total "+dformat.format(grandTotal).toString())
        totalAmount = dformat.format(grandTotal).toString()

    }


}