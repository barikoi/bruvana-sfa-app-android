package com.barikoi.cnlapp.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Model.Routes
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.MoreSpinner
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.*
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class MapFragment : Fragment(), OnMapReadyCallback, LocationEngineListener, PermissionsListener {
    var routesList: ArrayList<String>? = ArrayList()
    var allRouteList: ArrayList<Routes>? = ArrayList()
    var shopList: ArrayList<Shops>? = ArrayList()
    var recylerView: RecyclerView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var spinner : MoreSpinner? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var mMap: MapboxMap? = null
    private var mapView: MapView? = null
    lateinit var fab: FloatingActionButton
    private var locationEngine: LocationEngine? = null
    private var locationPlugin: LocationLayerPlugin? = null
    private var permissionsManager: PermissionsManager? = null
    private var userId: String? = ""
    internal lateinit var icon: Icon
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
        Mapbox.getInstance(ACTIVITY.applicationContext, getString(R.string.mapbox_access_token))
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        fab = view.findViewById(R.id.fab)
        Telemetry.disableOnUserRequest();
        mapView = view.findViewById(R.id.mapview)
        mapView!!.setStyleUrl(getString(R.string.map_view_styleUrl))
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)
        spinner = view.findViewById(R.id.spinnerRoutes)


        return view
    }
    fun getShopList(userId: String){
        allRouteList!!.clear()
        queue = RequestQueueSingleton.getInstance(mContext).getRequestQueue()
        routesList!!.clear()
        val request = StringRequest(
            Request.Method.GET,
            Api.route_outlet_list+"?sr_id="+userId,
            { response ->
                //success
                Log.d("RouteFrag", response)
                try {
                    val data = JSONObject(response)
                    val routesArray = data.getJSONArray("so-routes")
                    for (i in 0 until routesArray.length()){
                        val route = routesArray.getJSONObject(i)
                        val route_id = route.getString("id")
                        val route_name = route.getString("route_name")
                        val route_code = route.getString("route_code")
                        val territory_name = route.getString("territory_name")
                        routesList!!.add(route_name)
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
                            val distributor_office = outlet.getString("distributor_office")
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
                                    distributor_office,
                                    territory_name,
                                    latitude,
                                    longitude,
                                    route_name
                                )
                            )
                        }
                        Log.d("RouteList", "all 1 "+ shopList!!.size.toString())
                        allRouteList!!.add(Routes(route_id, route_code, route_name, "", "", "", shopList!!))
                        for (i in 0 until allRouteList!!.size) {
                            Log.d("RouteList", "all 2 "+ allRouteList!![i].route_name+" "+ allRouteList!![i].shopList.size.toString())
                        }
                    }
                    if (spinner != null) {
                        for (i in 0 until allRouteList!!.size) {
                            Log.d("RouteList", "all 3 "+ allRouteList!![i].route_name+" "+ allRouteList!![i].shopList.size.toString())
                        }
                        val adapter = ArrayAdapter(
                            mContext!!,
                            android.R.layout.simple_spinner_item, routesList!!
                        )
                        spinner!!.adapter = adapter

                    }

                }catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            { error ->
                //error
                Log.d("error", error.toString())
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
                        Log.d("Verify", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
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
        getShopList(userId!!)
    }
    fun setTaskDetails(){
        icon = IconFactory.getInstance(mContext!!).fromResource(R.drawable.map_marker)

        /*mMap?.addMarker(MarkerOptions().position(LatLng(taskLat!!, taskLon!!)).icon(icon))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(taskLat!!, taskLon!!), 15.0))*/
    }

    private fun plotMarker(p: Shops) {
        icon = IconFactory.getInstance(mContext!!).fromResource(R.drawable.map_marker)
        val shopname: String = p.shop_name
        val m = mMap!!.addMarker(
            MarkerOptions().position(LatLng(p.latitude, p.longitude))
                .icon(icon)
                .title(shopname)
        )
        placemarkermap!![p.shop_code] = m
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(p.latitude, p.longitude), 15.0))
    }
    @SuppressLint("MissingPermission")
    override fun onMapReady(mapboxMap: MapboxMap?) {
        mMap = mapboxMap
        //mMap!!.setStyle(Style.MAPBOX_STREETS)
        enableLocation()

        val uiSettings: UiSettings = mapboxMap!!.uiSettings
        uiSettings.setCompassEnabled(false)

        //setTaskDetails()
        //getShopList(userId!!)

        fab?.setOnClickListener(View.OnClickListener {
            if (locationEngine != null) {
                val lastLocation = locationEngine!!.lastLocation
                if (lastLocation != null) {
                    setCameraPosition(LatLng(lastLocation.latitude, lastLocation.longitude), 15.0)
                } else {
                    locationEngine!!.requestLocationUpdates()
                }
            } else {
                enableLocation()
            }
        })

        spinner!!.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                placemarkermap!!.clear()
                val shops: ArrayList<Shops> = ArrayList()
                if (shopList!!.size > 0){
                    mMap!!.clear()
                    Log.d("RouteList", "all routelist "+ routesList!!.size.toString()+" position"+position)
                    Log.d("RouteList", "all shops "+ shopList!!.size.toString())
                    for (j in 0 until shopList!!.size) {
                        Log.d("RouteList", "all shops for "+ shopList!![j].route_code)
                        if (shopList!![j].route_code.equals(routesList!![position])) {
                            //shops.add(shopList!![i])
                            plotMarker(shopList!![j])
                        }

                    }
                }

                /*val adapter = ShopListAdapter(shops)
                recylerView!!.setAdapter(adapter)
                adapter.notifyDataSetChanged()*/



            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(mContext!!)) {
            // Create an instance of LOST location engine
            initializeLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(Activity())
        }
    }
    @SuppressLint("MissingPermission")
    private fun initializeLocationEngine() {
        val locationEngineProvider = LocationEngineProvider(mContext!!.applicationContext)
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable()
        locationEngine!!.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine!!.activate()
        if (locationPlugin == null) {
            locationPlugin = LocationLayerPlugin(mapView!!, mMap!!, locationEngine, LocationLayerOptions.builder(mContext!!).maxZoom(25.0).build())
            locationPlugin!!.setLocationLayerEnabled(true)
            locationPlugin!!.renderMode = RenderMode.COMPASS
            locationPlugin!!.setCameraMode(CameraMode.TRACKING)
            Log.d("Search", "getLastLatLon 2: " + locationPlugin!!.lastKnownLocation)
            //mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15.0))
        }
        locationEngine!!.addLocationEngineListener(this)
        locationEngine!!.requestLocationUpdates()
        Log.d("Search", "getLastLatLon 2: " + locationEngine!!.lastLocation)

        val lastLocation = locationEngine!!.lastLocation
        if (lastLocation != null) {
            //setCameraPosition(LatLng(lastLocation.latitude, lastLocation.longitude), 15.0)
            locationEngine!!.removeLocationUpdates()
        } else {
            //Log.d("Search", "getLastLatLon: " +lastLocation.toString())
            locationEngine!!.addLocationEngineListener(this)
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


    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine!!.requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            /*taskerLat = location.latitude
            taskerLon = location.longitude*/
            if (mMap != null)
            /*IntentDataCheck()*/
                locationEngine!!.removeLocationEngineListener(this)
        } else {
            locationEngine!!.requestLocationUpdates()
        }
    }



    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
        enableLocation()
    }
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (locationEngine != null) {
            locationEngine!!.requestLocationUpdates()
        }
        if (locationPlugin != null) {
            locationPlugin!!.onStart()
        }

        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (locationEngine != null) {
            locationEngine!!.removeLocationUpdates()
        }
        if (locationPlugin != null) {
            locationPlugin!!.onStop()
        }
        mapView!!.onStop()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationEngine != null) {
            locationEngine!!.deactivate()
        }
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
}