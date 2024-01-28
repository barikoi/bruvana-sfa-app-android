package com.barikoi.cnlapp.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.pusher.client.channel.PrivateChannelEventListener
import com.pusher.client.channel.PusherEvent
import com.pusher.client.connection.ConnectionEventListener
import com.pusher.client.connection.ConnectionState
import com.pusher.client.connection.ConnectionStateChange
import com.pusher.client.util.HttpChannelAuthorizer
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_map.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException


class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    var routesList: ArrayList<Pair<String, String>>? = ArrayList()
    var routeNameList: ArrayList<String>? = ArrayList()
    var allRouteList: ArrayList<Routes>? = ArrayList()
    var shopList: ArrayList<Shops>? = ArrayList()
    var nonVerifiedShopList: ArrayList<Shops>? = ArrayList()
    var verifiedShopList: ArrayList<Shops>? = ArrayList()
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var spinner: MoreSpinner? = null
    var spinnerCategory: MoreSpinner? = null
    var cbVerified: AppCompatCheckBox? = null
    var cbTrace: AppCompatCheckBox? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var mMap: MapboxMap? = null
    private var mapView: MapView? = null
    lateinit var fab: FloatingActionButton
    lateinit var shopCount: TextView
    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null

    private var permissionsManager: PermissionsManager? = null
    private var userId: String? = ""
    private var srCode: String? = ""
    private var routeId: String? = ""
    private var routeName: String? = ""
    var token: String? = null
    var selected_so: Int? = null
    var selected_so_id: String? = null
    var selected_category: String? = null
    val soList: ArrayList<SOList> = ArrayList()
    var categoryList: ArrayList<String> = ArrayList()
    val filteredsoList: ArrayList<HistoryList> = ArrayList()
    internal lateinit var icon: Icon
    private var loading: ProgressBar? = null
    private var placemarkermap: java.util.HashMap<String, Marker>? = HashMap<String, Marker>()
    lateinit var ACTIVITY: MainActivity
    lateinit var pusher: Pusher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ACTIVITY = context as MainActivity

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Mapbox.getInstance(mContext!!, null)
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapview)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        fab = view.findViewById(R.id.fab)
        shopCount = view.findViewById(R.id.shopCount)
        cbVerified = view.findViewById(R.id.isVerified)
        cbTrace = view.findViewById(R.id.isTrace)
        loading = view.findViewById(R.id.progressBar1)

        spinner = view.findViewById(R.id.spinnerRoutes)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO")) {
            cbVerified!!.visibility = View.VISIBLE
            cbTrace!!.visibility = View.VISIBLE
        } else {
            cbVerified!!.visibility = View.GONE
            cbTrace!!.visibility = View.GONE
        }

        return view
    }

    private fun setupWebSocket() {
        val groupName =
            prefs!!.getString(Api.TRACE_GROUP_NAME, "")!!.toLowerCase().replace(" ", "_")

        val channelAuthorizer =
            HttpChannelAuthorizer("https://backend.barikoi.com:8888/api/broadcasting/auth")
        val params: MutableMap<String, String> = java.util.HashMap()
        val traceToken = prefs!!.getString(Api.TRACE_TOKEN, "")
        if (traceToken != "") {
            params["Authorization"] = "bearer $traceToken"
        }
        channelAuthorizer.setHeaders(params)
        val options = PusherOptions()
        options.setCluster("ap2").setChannelAuthorizer(channelAuthorizer)
        options.setWsPort(6001)
        options.setWssPort(6002)
        options.setHost("backend.barikoi.com")
        options.isUseTLS = true
        pusher = Pusher("mykey", options)
        pusher.connect()
        pusher.connect(object : ConnectionEventListener {
            override fun onConnectionStateChange(change: ConnectionStateChange) {
                println("State changed from ${change.previousState} to ${change.currentState}")
            }

            override fun onError(
                message: String,
                code: String,
                e: Exception
            ) {
                println("There was a problem connecting! code ($code), message ($message), exception($e)")
            }
        }, ConnectionState.ALL)

        pusher.subscribePrivate(
            "private-care_nutrition_39752",
            object : PrivateChannelEventListener {
                override fun onSubscriptionSucceeded(channelName: String) {
                    println("Subscribed! " + channelName)
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
                    println("onAuthenticationFailure: $message")
                }

                override fun onEvent(event: PusherEvent?) {
                    println("Received event with data: $event")
                }
            })
            .bind("care_nutrition_group_event_" + groupName, object : PrivateChannelEventListener {
                override fun onEvent(event: PusherEvent?) {
                    println("Received event with data bind: $event")
                    val dataObj = JSONObject(event!!.data).getJSONObject("data")
                    println("Received data bind: $dataObj")
                    val userName = dataObj.getString("name")
                    println("Received userName bind: $userName")
                    val latitude = dataObj.getDouble("latitude")
                    val longitude = dataObj.getDouble("longitude")
                    val time = dataObj.getString("updated_at")
                    val iconTrace: Icon
                    if (dataObj.getInt("active_status") == 1) {
                        iconTrace = IconFactory.getInstance(mContext!!)
                            .fromResource(R.drawable.trace_active)

                    } else {
                        iconTrace = IconFactory.getInstance(mContext!!)
                            .fromResource(R.drawable.trace_inactive)
                    }
                    ACTIVITY.runOnUiThread {
                        plotTraceUser(userName, time, latitude, longitude, iconTrace)
                    }
                }

                override fun onSubscriptionSucceeded(channelName: String?) {
                    println("onSubscriptionSucceeded: $channelName")
                }

                override fun onAuthenticationFailure(message: String?, e: java.lang.Exception?) {
                    println("onAuthenticationFailure bind: $message")
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)) {
            spinnerLayoutSO.visibility = View.VISIBLE
            getSOList()
        } else {
            spinnerLayoutSO.visibility = View.GONE
        }

        spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerSO.adapter.count > 0) {
                    selected_so = p2
                    userId = soList[p2].id
                    srCode = soList[p2].employeeId
                    if (cbVerified!!.isChecked) {
                        if (srCode!!.length > 0) {
                            getShopList(
                                Api.verified_shop_list + "?verified_outlets=1&route_id=" + routeId + "&user_id=" + userId,
                                "start"
                            )
                        }
                    } else {
                        if (userId!!.length > 0) {
                            getShopList(
                                Api.routes_withfilter + "?user_id=" + userId + "&with_outlets=1",
                                "start"
                            )
                        }
                    }
                    if (cbTrace!!.isChecked) {
                        setupWebSocket()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        spinner!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                placemarkermap!!.clear()
                //loading!!.visibility = View.VISIBLE
                routeId = routesList!!.get(position).first
                routeName = routesList!!.get(position).second
                //val shops: ArrayList<Shops> = ArrayList()
                if (cbVerified!!.isChecked) {
                    mMap!!.clear()
                    if (srCode!!.length > 0) {
                        getShopList(
                            Api.verified_shop_list + "?verified_outlets=1&route_id=" + routeId + "&user_id=" + userId,
                            ""
                        )
                    }
                } else {
                    nonVerifiedShopList!!.clear()
                    if (shopList!!.size > 0) {
                        mMap!!.clear()
                        Log.d(
                            "RouteList",
                            "all routelist " + routesList!!.size.toString() + " position" + position
                        )
                        Log.d("RouteList", "all shops " + shopList!!.size.toString())
                        for (j in 0 until shopList!!.size) {
                            Log.d("RouteList", "all shops for " + shopList!![j].route_code)
                            if (shopList!![j].route_name.equals(routesList!![position].second)) {
                                //shops.add(shopList!![i])
                                if (selected_category != null) {
                                    if (selected_category.equals("All")) {
                                        nonVerifiedShopList!!.add(shopList!![j])
                                        icon = IconFactory.getInstance(mContext!!)
                                            .fromResource(R.drawable.map_marker_red)
                                        plotMarker(shopList!![j], icon)
                                    } else if (shopList!![j].category.equals(
                                            selected_category,
                                            true
                                        )
                                    ) {
                                        nonVerifiedShopList!!.add(shopList!![j])
                                        icon = IconFactory.getInstance(mContext!!)
                                            .fromResource(R.drawable.map_marker_red)
                                        plotMarker(shopList!![j], icon)
                                    }
                                }
                            }

                        }
                        shopCount.setText("${nonVerifiedShopList!!.size} outlet(s)")
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        categoryList = arrayListOf<String>("All", "A", "B", "C", "D", "E", "F", "P", "MP", "WS")
        val adapter = ArrayAdapter(
            mContext!!,
            android.R.layout.simple_spinner_item, categoryList!!
        )
        spinnerCategory!!.adapter = adapter
        spinnerCategory!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {

                selected_category = categoryList.get(position)
                if (shopList!!.size > 0) {
                    placemarkermap!!.clear()
                    //loading!!.visibility = View.VISIBLE
                    //val shops: ArrayList<Shops> = ArrayList()
                    if (cbVerified!!.isChecked) {
                        mMap!!.clear()
                        if (srCode!!.length > 0) {
                            getShopList(
                                Api.verified_shop_list + "?verified_outlets=1&route_id=" + routeId + "&user_id=" + userId,
                                ""
                            )
                        }
                    } else {
                        nonVerifiedShopList!!.clear()
                        if (shopList!!.size > 0) {
                            mMap!!.clear()
                            Log.d("RouteList", "all shops " + shopList!!.size.toString())
                            for (j in 0 until shopList!!.size) {
                                Log.d("RouteList", "all shops for " + shopList!![j].route_code)
                                if (shopList!![j].route_name.equals(routeName)) {
                                    if (selected_category != null) {
                                        if (selected_category.equals("All")) {
                                            nonVerifiedShopList!!.add(shopList!![j])
                                            icon = IconFactory.getInstance(mContext!!)
                                                .fromResource(R.drawable.map_marker_red)
                                            plotMarker(shopList!![j], icon)
                                        } else if (shopList!![j].category.equals(
                                                selected_category,
                                                true
                                            )
                                        ) {
                                            nonVerifiedShopList!!.add(shopList!![j])
                                            icon = IconFactory.getInstance(mContext!!)
                                                .fromResource(R.drawable.map_marker_red)
                                            plotMarker(shopList!![j], icon)
                                        }
                                    }
                                    //shops.add(shopList!![i])

                                }

                            }
                            shopCount.setText("${nonVerifiedShopList!!.size} outlet(s)")
                        }
                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }



        cbVerified!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                mMap!!.clear()
                if (srCode!!.length > 0) {
                    getShopList(
                        Api.verified_shop_list + "?verified_outlets=1&route_id=" + routeId + "&user_id=" + userId,
                        "checkbox"
                    )
                }
            } else {
                mMap!!.clear()
                //getShopList(Api.route_outlet_list+"?sr_id="+userId)
                if (shopList!!.size > 0) {
                    nonVerifiedShopList!!.clear()
                    mMap!!.clear()
                    Log.d("RouteList", "spinner selected " + spinner!!.selectedItem)
                    Log.d("RouteList", "all shops " + shopList!!.size.toString())
                    for (j in 0 until shopList!!.size) {
                        Log.d("RouteList", "all shops for " + shopList!![j].route_code)
                        if (shopList!![j].route_name.equals(spinner!!.selectedItem)) {
                            if (selected_category != null) {
                                if (selected_category.equals("All")) {
                                    nonVerifiedShopList!!.add(shopList!![j])
                                    icon = IconFactory.getInstance(mContext!!)
                                        .fromResource(R.drawable.map_marker_red)
                                    plotMarker(shopList!![j], icon)
                                } else if (shopList!![j].category.equals(selected_category, true)) {
                                    nonVerifiedShopList!!.add(shopList!![j])
                                    icon = IconFactory.getInstance(mContext!!)
                                        .fromResource(R.drawable.map_marker_red)
                                    plotMarker(shopList!![j], icon)
                                }
                            }
                        }

                    }
                    shopCount.setText("${nonVerifiedShopList!!.size} outlet(s)")
                }
            }
        })
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                    //setDateFilter()
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
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun viewSOList(response: String) {
        try {
            if (response != null) {
                soList.clear()
                val obj = JSONObject(response)
                val toArray = obj.getJSONArray("so_list")
                val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
                val soNameList: ArrayList<String> = ArrayList()
                if (soArray.length() > 0) {
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        var imageUrl = "null"
                        if (soObj.has("images") && !soObj.isNull("images")) {
                            val imageArray = soObj.getJSONArray("images")
                            if (imageArray.length() > 0) {
                                val imageobj = imageArray.getJSONObject(0)
                                if (imageobj.has("image_url")) {
                                    imageUrl = imageobj.getString("image_url")
                                }
                            }
                        }
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("user_name"),
                                soObj.getString("designation"),
                                if (soObj.has("employee_id")) soObj.getString("employee_id") else "",
                                imageUrl
                            )
                        )
                        soNameList.add(soObj.getString("user_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    mContext!!,
                    android.R.layout.simple_spinner_item, soNameList
                )
                spinnerSO.adapter = adapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getShopList(url: String, from: String) {
        loading!!.visibility = View.VISIBLE
        allRouteList!!.clear()
        //routesList!!.clear()
        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                //success
                Log.d("RouteFrag", response)
                try {
                    loading!!.visibility = View.GONE
                    verifiedShopList!!.clear()
                    //allRouteList!!.clear()
                    val data = JSONObject(response)
                    if (data.has("routes")) {
                        shopList!!.clear()
                        routesList!!.clear()
                        routeNameList!!.clear()
                        val routesArray = data.getJSONArray("routes")
                        for (i in 0 until routesArray.length()) {
                            val route = routesArray.getJSONObject(i)
                            val route_id = route.getString("id")
                            val route_name = route.getString("route_name")
                            val route_code = route.getString("route_code")
                            val territory_name = route.getString("territory_name")
                            routesList!!.add(Pair(route_id, route_name))
                            routeNameList!!.add(route_name)
                            //shopList!!.clear()
                            val route_outlet_list = route.getJSONArray("outlets")
                            for (j in 0 until route_outlet_list.length()) {
                                val outlet = route_outlet_list.getJSONObject(j)
                                val outlet_id = outlet.getString("id")
                                val outlet_name = outlet.getString("outlet_name")
                                val outlet_status = outlet.getString("outlet_status")
                                val outlet_address = outlet.getString("address")
                                val outlet_code = outlet.getString("outlet_code")
                                val outlet_type = outlet.getString("outlet_type")
                                /*val outlet_category = outlet.getString("outlet_category")*/
                                val owner_name = outlet.getString("owner_name")
                                val min_order = ""
                                val market_opportunity = outlet.getString("market_opportunity")
                                val contact_number = outlet.getString("phone_number")
                                val is_buyer = outlet.getInt("is_buyer")
                                /*val distributor_office = outlet.getString("distributor_office")
                                val distributor_office_code = outlet.getString("distributor_office_code")*/
                                val outlet_category = outlet.getString("outlet_category")
                                val latitude = outlet.getDouble("latitude")
                                val longitude = outlet.getDouble("longitude")
                                val is_Verified = outlet.getInt("is_verified")
                                shopList!!.add(
                                    Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        outlet_category,
                                        owner_name,
                                        min_order,
                                        market_opportunity,
                                        contact_number,
                                        is_buyer,
                                        "",
                                        ArrayList(),
                                        /*distributor_office,
                                distributor_office_code,*/
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        is_Verified, 0, 0
                                    )
                                )
                            }
                            Log.d("RouteList", "all 1 " + shopList!!.size.toString())
                        }
                    } else if (data.has("outlets")) {
                        val routesOutletArray = data.getJSONArray("outlets")
                        for (i in 0 until routesOutletArray.length()) {
                            val outlet = routesOutletArray.getJSONObject(i)
                            val outlet_id = outlet.getString("id")
                            val outlet_name = outlet.getString("outlet_name")
                            val outlet_status = outlet.getString("outlet_status")
                            val outlet_address = outlet.getString("address")
                            val outlet_code = outlet.getString("outlet_code")
                            val outlet_type = outlet.getString("outlet_type")
                            val owner_name = outlet.getString("owner_name")
                            val market_opportunity = outlet.getString("market_opportunity")
                            val contact_number = outlet.getString("phone_number")
                            val is_buyer = outlet.getInt("is_buyer")
                            /*val distributor_office = outlet.getString("distributor_office")
                            val distributor_office_code = outlet.getString("distributor_office_code")*/
                            val outlet_category = outlet.getString("outlet_category")
                            val latitude = outlet.getDouble("latitude")
                            val longitude = outlet.getDouble("longitude")
                            val route_id = outlet.getString("route_id")
                            val route_name = outlet.getString("route_name")
                            val territory_name = outlet.getString("territory_name")
                            val is_Verified = outlet.getInt("is_verified")

                            if (selected_category != null) {
                                if (selected_category.equals("All")) {
                                    val shops = Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        outlet_category,
                                        owner_name,
                                        "",
                                        market_opportunity,
                                        contact_number,
                                        is_buyer,
                                        "",
                                        ArrayList(),
                                        /*distributor_office,
                                        distributor_office_code,*/
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        is_Verified, 0, 0
                                    )
                                    verifiedShopList!!.add(shops)
                                    icon = IconFactory.getInstance(mContext!!)
                                        .fromResource(R.drawable.map_marker_green)
                                    plotMarker(shops, icon)
                                } else if (outlet_category.equals(selected_category, true)) {
                                    val shops = Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        outlet_category,
                                        owner_name,
                                        "",
                                        market_opportunity,
                                        contact_number,
                                        is_buyer,
                                        "",
                                        ArrayList(),
                                        /*distributor_office,
                                        distributor_office_code,*/
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        is_Verified, 0, 0
                                    )
                                    verifiedShopList!!.add(shops)
                                    icon = IconFactory.getInstance(mContext!!)
                                        .fromResource(R.drawable.map_marker_green)
                                    plotMarker(shops, icon)
                                }
                            }

                        }
                        shopCount.setText("${verifiedShopList!!.size} outlet(s)")
                        Log.d("RouteList", "all verified 1 " + verifiedShopList!!.size.toString())
                        Log.d("RouteList", "all verified 1 " + allRouteList!!.size.toString())

                    }

                    if (from.equals("start")) {
                        if (spinner != null) {
                            val adapter = ArrayAdapter(
                                mContext!!,
                                android.R.layout.simple_spinner_item, routeNameList!!
                            )
                            spinner!!.adapter = adapter

                        }
                    }

                } catch (e: JSONException) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }

            },
            { error ->
                //error
                Log.d("error", error.toString())
                loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    Toast.makeText(
                        mContext,
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    Toast.makeText(
                        mContext,
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Map", "message: $s")
                        val data = JSONObject(s)
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG)
                            .show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
        )
        queue!!.add(request)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        /*token = prefs.getString("token", "")
        user_id = prefs.getString("user_id", "")*/
        mContext = context
        userId = prefs!!.getString(Api.USER_ID, "")
        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO")) {
            userId = ""
            srCode = ""
        } else {
            userId = prefs!!.getString(Api.USER_ID, "")
            srCode = prefs!!.getString(Api.EMPLOYEE_ID, "")
        }

        token = prefs!!.getString(Api.TOKEN, "")
    }

    private fun plotTraceUser(
        userName: String,
        time: String,
        lat: Double,
        lon: Double,
        icon: Icon
    ) {
        mMap!!.clear()
        mMap!!.addMarker(
            MarkerOptions().position(LatLng(lat, lon))
                .icon(icon)
                .title(userName + "| " + time)
        )
        //placemarkermap!![p.shop_code] = m
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 17.0))
    }

    private fun plotMarker(p: Shops, icon: Icon) {

        val shopname: String = p.shop_name
        val m = mMap!!.addMarker(
            MarkerOptions().position(LatLng(p.latitude, p.longitude))
                .icon(icon)
                .title(p.category + ", " + shopname)
        )
        placemarkermap!![p.shop_code] = m
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 12.0))
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(mContext!!)) {
            // Create an instance of LOST location engine
            //initializeLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(Activity())
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(mContext!!)) {

            // Get an instance of the component
            val locationComponent = mMap!!.locationComponent

            // Activate with options
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(mContext!!, loadedMapStyle)
                    .build()
            )
            // Enable to make component visible
            locationComponent.isLocationComponentEnabled = true

            // Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING

            // Set the component's render mode
            locationComponent.renderMode = RenderMode.COMPASS
            locationEngineRequest = locationComponent.locationEngineRequest
            locationEngine = locationComponent.locationEngine
            if (locationEngine != null) {
                locationEngine!!.getLastLocation(object :
                    LocationEngineCallback<LocationEngineResult?> {

                    override fun onSuccess(result: LocationEngineResult?) {
                        val lastLocation: Location = result!!.getLastLocation()!!
                        if (lastLocation != null && !lastLocation.equals("null")) {
                            //setCameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 17.0);
                        } else {
                            //locationEngine!!.requestLocationUpdates(locationEngineRequest!!, null)
                            showEnableLocationSetting(ACTIVITY)
                        }
                    }

                    override fun onFailure(exception: java.lang.Exception) {
                        Toast.makeText(
                            mContext!!,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        } else {
            permissionsManager = PermissionsManager(this as PermissionsListener)
            permissionsManager!!.requestLocationPermissions(ACTIVITY)
            //showEnableLocationSetting(this@CreateShopActivity)
        }
    }

    private fun showEnableLocationSetting(activity: MainActivity) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val task =
            LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        task.addOnSuccessListener(
            activity
        ) { response ->
            val states = response.locationSettingsStates
            if (states!!.isLocationPresent) {
                //Do something
            }
        }
        task.addOnFailureListener(activity) { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity, 999)
                } catch (ex: SendIntentException) {
                    ex.printStackTrace()
                }
            }
        }
    }


    private fun setCameraPosition(location: LatLng, zoom: Double?) {
        mMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), zoom!!
            )
        )
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
        mMap!!.getStyle(object : Style.OnStyleLoaded {
            override fun onStyleLoaded(style: Style) {
                if (granted) {
                    enableLocationComponent(style)
                } else {
                    Toast.makeText(
                        mContext!!,
                        "Permission not granted",
                        Toast.LENGTH_LONG
                    ).show()
                    //finish()
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
        pusher.disconnect()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
        pusher.disconnect()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        Log.d("Verify", "onResume")

    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
        Log.d("Verify", "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView!!.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mMap = mapboxMap
        mMap!!.setStyle(
            Style.Builder().fromUrl(getString(R.string.map_view_styleUrl))
        ) { style: Style? -> enableLocationComponent(style!!) }

        val uiSettings: UiSettings = mapboxMap!!.uiSettings
        uiSettings.setCompassEnabled(false)
        mMap!!.setMaxZoomPreference(25.5)
        if (userId!!.length > 0) {
            getShopList(Api.routes_withfilter + "?user_id=" + userId + "&with_outlets=1", "start")
        }

        fab.setOnClickListener(View.OnClickListener {
            /*if (locationEngine != null) {
                val lastLocation = locationEngine!!.lastLocation
                if (lastLocation != null) {
                    setCameraPosition(LatLng(lastLocation.latitude, lastLocation.longitude), 15.0)
                } else {
                    locationEngine!!.requestLocationUpdates()
                }
            } else {
                enableLocation()
            }*/
        })

        //setupWebSocket()


    }
}