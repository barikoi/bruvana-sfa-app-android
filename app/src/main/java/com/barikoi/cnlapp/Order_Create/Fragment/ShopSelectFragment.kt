package com.barikoi.cnlapp.Order_Create.Fragment

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
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
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Adapter.ShopSelectAdapter
import com.barikoi.cnlapp.Order_Create.Callback.OnSelectListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_shop_select.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class ShopSelectFragment : Fragment(), OnSelectListener {
    var recylerView: RecyclerView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var spinner : MoreSpinner? = null
    var user_id : String? = null
    var listener: OnSelectListener? = null
    var et_search: AutoCompleteTextView? = null
    private var adapter: ShopSelectAdapter? = null
    //var routeList: ArrayList<Routes>? = ArrayList()
    var shopList: ArrayList<Shops>? = ArrayList()
    var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var loading: ProgressBar? = null
    lateinit var ACTIVITY: MainActivity
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gd = GradientDrawable()
        gd.setColor(mContext!!.resources.getColor(R.color.white))
        gd.cornerRadius = 5f
        gd.setStroke(2, mContext!!.resources.getColor(R.color.cnl_color_2))
        createShop.setBackgroundDrawable(gd)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_shop_select, container, false)
        recylerView = view.findViewById(R.id.shoplist)
        loading = view.findViewById(R.id.progressBar)
        spinner = view.findViewById(R.id.spinnerRoutes)
        et_search = view.findViewById(R.id.editTextSearchShop)
        adapter = ShopSelectAdapter( ArrayList<Shops>(), listener!!)
        recylerView!!.adapter = adapter


        spinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                for (i in 0 until routeNameList!!.size) {
                    Log.d("RouteList", "all 2 "+ routeNameList!![i].second)
                }
                Log.d("RouteList", "position: "+p2)
                Log.d("RouteList", "size: "+routeNameList!!.size)
                val route_id = routeNameList!![p2].first
                editor!!.putString(Api.SELECTED_ROUTE_ID, route_id)
                editor!!.commit()
                getShopListbyRoute(Api.routes_withfilter+"?with_geometry=0&with_outlets=1&route_id="+route_id+"&sr_id="+user_id)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        et_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /*if (s!!.length>0){
                    adapter!!.filter.filter(s)
                }else{
                    getShopList(userId!!)
                }*/
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    val shops: ArrayList<Shops> = ArrayList()
                    for (i in 0 until shopList!!.size) {
                        if (shopList!![i].route_name == routeNameList!![spinner!!.selectedItemPosition].second) {
                            shops.add(shopList!![i])
                        }

                    }
                    adapter!!.shopList=shops
                    adapter!!.notifyDataSetChanged()
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        //getLocation2()

        return view
    }

    private fun getAllRoutes(url: String) {
        //loading!!.visibility = View.VISIBLE
        routeNameList!!.clear()
        val request = StringRequest(Request.Method.GET, url,
            {
                response ->
                try {
                    //loading!!.visibility = View.GONE
                    val data = JSONObject(response)
                    if (data.has("routes") && !data.isNull("routes")){
                        val routesList = ArrayList<String>()
                        val routesArray = data.getJSONArray("routes")
                        if (routesArray.length() > 0){

                            for(i in 0 until routesArray.length()){
                                val routeObj = routesArray.getJSONObject(i)

                                routeNameList!!.add(Pair(routeObj.getString("id"), routeObj.getString("route_name")))
                                routesList.add(routeObj.getString("route_name"))
                            }
                            /*for (i in 0 until ShopListFragment.allRouteList!!.size) {
                                Log.d("RouteList", "all 2 "+ ShopListFragment.allRouteList!![i].route_name+" "+ ShopListFragment.allRouteList!![i].shopList.size.toString())
                            }*/
                            if (spinner != null) {
                                if (spinner!!.adapter == null){
                                    val adapter = ArrayAdapter(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routesList
                                    )
                                    spinner!!.adapter = adapter
                                }

                            }
                        }

                    }
                }catch (e:Exception){
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                //loading!!.visibility = View.GONE
                if (error is TimeoutError) {
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

    fun getLocation2(){
        val lm = mContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext!!)
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            Log.d("ConfirmOrder", "Location: "+mLocationRequest)
            mLocationCallback = object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        if (!location.isFromMockProvider) {
                            Log.d("ConfirmOrder", "Location: "+location)
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
            //showGPSDisabledAlertToUser()
        }
    }

    private fun getShopListbyRoute(url: String) {
        loading!!.visibility = View.VISIBLE
        val request = StringRequest(Request.Method.GET, url,
            {
                    response ->
                try {
                    loading!!.visibility = View.GONE

                    val data = JSONObject(response)
                    if (data.has("routes") && !data.isNull("routes")){
                        val routesArray = data.getJSONArray("routes")
                        if (routesArray.length() > 0){
                            shopList!!.clear()
                            val routeObj = routesArray.getJSONObject(0)
                            /*val route = Routes(routeObj.getString("id"),
                                routeObj.getString("route_code"),
                                routeObj.getString("route_name"),
                                routeObj.getString("territory_name"),
                                routeObj.getString("area_name"),
                                "",
                                ArrayList<Shops>()
                            )*/

                            val outletsArray = routeObj.getJSONArray("outlets")
                            if (outletsArray.length()>0){

                                for (i in 0 until outletsArray.length()){
                                    val outletObj = outletsArray.getJSONObject(i)
                                    val shops = Shops(
                                        outletObj.getString("id"),
                                        outletObj.getString("outlet_name"),
                                        outletObj.getString("outlets_status"),
                                        outletObj.getString("address"),
                                        outletObj.getString("outlet_code"),
                                        outletObj.getString("store_type"),
                                        outletObj.getString("outlet_category"),
                                        outletObj.getString("owner_name"),
                                        outletObj.getString("distributor_office"),
                                        outletObj.getString("distributor_office_code"),
                                        routeObj.getString("territory_name"),
                                        outletObj.getDouble("latitude"),
                                        outletObj.getDouble("longitude"),
                                        routeObj.getString("route_code"),
                                        routeObj.getString("route_name"),
                                        outletObj.getString("order_delivery_date"),
                                        1
                                    )

                                    shopList!!.add(shops)
                                }

                                if (shopList!!.size > 0){
                                    val adapter = ShopSelectAdapter(shopList!!, listener!!)
                                    recylerView!!.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            if (spinner != null) {
                                if (spinner!!.adapter == null){
                                    val adapter = ArrayAdapter(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routeNameList!!
                                    )
                                    spinner!!.adapter = adapter
                                }

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
        mContext = context
        listener = this

        ACTIVITY = context as MainActivity

        getAllRoutes(Api.routes_withfilter+"?with_geometry=0&sr_id="+user_id)
    }

    override fun onShopSelected(shop: Shops) {
        editor!!.putString(Api.SELECTED_SHOP_ID, shop.shop_id)
        editor!!.commit()
        CreateOrderFragment.startFragmentWithValue("Shop", shop, ProductSelectFragment(), ACTIVITY)
    }
}