package com.barikoi.cnlapp.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.Fragment
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_history_t_o.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.spinnerSO
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
    var recylerView: RecyclerView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var spinner : MoreSpinner? = null
    var cbVerified: AppCompatCheckBox? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var mMap: MapboxMap? = null
    private var mapView: MapView? = null
    lateinit var fab: FloatingActionButton
    lateinit var shopCount: TextView
    private var locationEngine: LocationEngine? = null
    //private var locationPlugin: LocationLayerPlugin? = null
    private var permissionsManager: PermissionsManager? = null
    private var userId: String? = ""
    private var srCode: String? = ""
    private var routeId: String? = ""
    var token : String? = null
    var selected_so : Int? = null
    var selected_so_id : String? = null
    val soList: ArrayList<SOList> = ArrayList()
    val filteredsoList: ArrayList<HistoryList> = ArrayList()
    internal lateinit var icon: Icon
    private var loading: ProgressBar? = null
    private var placemarkermap: java.util.HashMap<String, Marker>? = HashMap<String, Marker>()
    lateinit var ACTIVITY: MainActivity

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
        loading = view.findViewById(R.id.progressBar1)

        spinner = view.findViewById(R.id.spinnerRoutes)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)){
            spinnerLayoutSO.visibility = View.VISIBLE
            getSOList()
        }else{
            spinnerLayoutSO.visibility = View.GONE
        }

        spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerSO.adapter.count >0) {
                    selected_so = p2
                    userId = soList[p2].id
                    srCode = soList[p2].employeeId
                    if (cbVerified!!.isChecked){
                        if (srCode!!.length > 0) {
                            getShopList(Api.verified_shop_list + "?route_id=" + routeId + "&sr_code=" + srCode)
                        }
                    }else{
                        if (userId!!.length > 0) {
                            getShopList(Api.route_outlet_list + "?sr_id=" +userId)
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        spinner!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                placemarkermap!!.clear()
                //loading!!.visibility = View.VISIBLE
                routeId = routesList!!.get(position).first
                //val shops: ArrayList<Shops> = ArrayList()
                if (cbVerified!!.isChecked){
                    mMap!!.clear()
                    if (srCode!!.length > 0) {
                        getShopList(Api.verified_shop_list + "?route_id=" + routeId + "&sr_code=" + srCode)
                    }
                }else{
                    nonVerifiedShopList!!.clear()
                    if (shopList!!.size > 0){
                        mMap!!.clear()
                        Log.d("RouteList", "all routelist "+ routesList!!.size.toString()+" position"+position)
                        Log.d("RouteList", "all shops "+ shopList!!.size.toString())
                        for (j in 0 until shopList!!.size) {
                            Log.d("RouteList", "all shops for "+ shopList!![j].route_code)
                            if (shopList!![j].route_name.equals(routesList!![position].second)) {
                                //shops.add(shopList!![i])
                                nonVerifiedShopList!!.add(shopList!![j])
                                icon = IconFactory.getInstance(mContext!!).fromResource(R.drawable.map_marker_red)
                                plotMarker(shopList!![j], icon)
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

        cbVerified!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                mMap!!.clear()
                if (srCode!!.length > 0) {
                    getShopList(Api.verified_shop_list + "?route_id=" + routeId + "&sr_code=" + srCode)
                }
            } else {
                mMap!!.clear()
                //getShopList(Api.route_outlet_list+"?sr_id="+userId)
                if (shopList!!.size > 0){
                    nonVerifiedShopList!!.clear()
                    mMap!!.clear()
                    Log.d("RouteList", "spinner selected "+ spinner!!.selectedItem)
                    Log.d("RouteList", "all shops "+ shopList!!.size.toString())
                    for (j in 0 until shopList!!.size) {
                        Log.d("RouteList", "all shops for "+ shopList!![j].route_code)
                        if (shopList!![j].route_name.equals(spinner!!.selectedItem)) {
                            nonVerifiedShopList!!.add(shopList!![j])
                            icon = IconFactory.getInstance(mContext!!).fromResource(R.drawable.map_marker_red)
                            plotMarker(shopList!![j], icon)
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
            if (response != null){
                soList.clear()
                val obj = JSONObject(response)
                val soArray = obj.getJSONArray("so")
                val soNameList: ArrayList<String> = ArrayList()
                if (soArray.length() >0){
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("name"),
                                soObj.getString("designation"),
                                soObj.getString("employee_id"),
                                soObj.getString("phone"),
                                soObj.getString("image")
                            )
                        )
                        soNameList.add(soObj.getString("name"))

                    }
                }
                val adapter = ArrayAdapter(
                    mContext!!,
                    android.R.layout.simple_spinner_item, soNameList
                )
                spinnerSO.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun getShopList(url: String){
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
                    if (data.has("so-routes")){
                        shopList!!.clear()
                        routesList!!.clear()
                        routeNameList!!.clear()
                        val routesArray = data.getJSONArray("so-routes")
                        for (i in 0 until routesArray.length()){
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
                                val outlet_status = outlet.getString("outlets_status")
                                val outlet_address = outlet.getString("address")
                                val outlet_code = outlet.getString("outlet_code")
                                val outlet_type = outlet.getString("store_type")
                                /*val outlet_category = outlet.getString("outlet_category")*/
                                val owner_name = outlet.getString("owner_name")
                                val distributor_office = outlet.getString("distributor_office")
                                val distributor_office_code = outlet.getString("distributor_office_code")
                                val latitude = outlet.getDouble("latitude")
                                val longitude = outlet.getDouble("longitude")

                                shopList!!.add(
                                    Shops(
                                        outlet_id,
                                        outlet_name,
                                        outlet_status,
                                        outlet_address,
                                        outlet_code,
                                        outlet_type,
                                        "",
                                        owner_name,
                                        distributor_office,
                                        distributor_office_code,
                                        territory_name,
                                        latitude,
                                        longitude,
                                        route_id,
                                        route_name,
                                        "",
                                        0,0
                                    )
                                )
                            }
                            Log.d("RouteList", "all 1 "+ shopList!!.size.toString())
                        }
                    }else if (data.has("verified_outlet")){
                        val routesOutletArray = data.getJSONArray("verified_outlet")
                        for (i in 0 until routesOutletArray.length()){
                            val outlet = routesOutletArray.getJSONObject(i)
                            val outlet_id = outlet.getString("id")
                            val outlet_name = outlet.getString("outlet_name")
                            val outlet_status = outlet.getString("outlets_status")
                            val outlet_address = outlet.getString("address")
                            val outlet_code = outlet.getString("outlet_code")
                            val outlet_type = outlet.getString("store_type")
                            /*val outlet_category = outlet.getString("outlet_category")*/
                            val owner_name = outlet.getString("owner_name")
                            val distributor_office = outlet.getString("distributor_office")
                            val distributor_office_code = outlet.getString("distributor_office_code")
                            val latitude = outlet.getDouble("latitude")
                            val longitude = outlet.getDouble("longitude")
                            val route_id = outlet.getString("route_id")
                            val route_name = outlet.getString("route_name")
                            val territory_name = outlet.getString("territory_name")

                            val shops = Shops(
                                outlet_id,
                                outlet_name,
                                outlet_status,
                                outlet_address,
                                outlet_code,
                                outlet_type,
                                "",
                                owner_name,
                                distributor_office,
                                distributor_office_code,
                                territory_name,
                                latitude,
                                longitude,
                                route_id,
                                route_name,
                                "",
                                0, 0
                            )
                            verifiedShopList!!.add(shops)
                            icon = IconFactory.getInstance(mContext!!).fromResource(R.drawable.map_marker_green)
                            plotMarker(shops, icon)
                            }
                        shopCount.setText("${verifiedShopList!!.size} outlet(s)")
                        Log.d("RouteList", "all verified 1 "+ verifiedShopList!!.size.toString())
                        Log.d("RouteList", "all verified 1 "+ allRouteList!!.size.toString())

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

                }catch (e: JSONException) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }

            },
            { error ->
                //error
                Log.d("error", error.toString())
                loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
                }
                if (error is NoConnectionError) {
                    Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Map", "message: $s")
                        val data = JSONObject(s)
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
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
        if (prefs!!.getString(Api.USER_TYPE,"").equals("TO")){
            userId = ""
        }else{
            userId = prefs!!.getString(Api.USER_ID, "")
        }
        if (prefs!!.getString(Api.USER_TYPE,"").equals("TO")){
            srCode = ""
        }else{
            srCode = prefs!!.getString(Api.SR_CODE, "")
        }

        token = prefs!!.getString(Api.TOKEN, "")
    }

    private fun plotMarker(p: Shops, icon: Icon) {

        val shopname: String = p.shop_name
        val m = mMap!!.addMarker(
            MarkerOptions().position(LatLng(p.latitude, p.longitude))
                .icon(icon)
                .title(shopname)
        )
        placemarkermap!![p.shop_code] = m
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 15.0))
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
        enableLocation()
    }
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
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
        mMap!!.setStyle(Style.Builder().fromUrl(getString(R.string.map_view_styleUrl)))
        enableLocation()

        val uiSettings: UiSettings = mapboxMap!!.uiSettings
        uiSettings.setCompassEnabled(false)
        if (userId!!.length > 0) {
            getShopList(Api.route_outlet_list + "?sr_id=" + userId)
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


    }
}