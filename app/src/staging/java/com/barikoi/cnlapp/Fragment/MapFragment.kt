@file:Suppress("DEPRECATION", "UNUSED_VARIABLE")

package com.barikoi.cnlapp.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.model.socket.GroupUser
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.data.remote.models.SalesOfficer
import com.barikoi.cnlapp.databinding.FragmentMapBinding
import com.barikoi.cnlapp.socket.SocketHandler
import com.barikoi.cnlapp.socket.model.SocketResponse
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Api.TRACE_GROUP_ID
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.convertDate
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.gson.Gson
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private val viewModel: MapViewModel by viewModels()

    private lateinit var binding: FragmentMapBinding

    private lateinit var mMap: MapboxMap

    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null

    private var permissionsManager: PermissionsManager? = null

    private var soNewList: List<SalesOfficer> = emptyList()
    private var selectedSo: SalesOfficer? = null

    private var routeNewList: List<Route> = emptyList()
    private var routeID = ""

    private var socketLocalUserList: List<GroupUser> = emptyList()

    private var categoryList: ArrayList<String> = ArrayList()
    private var selectedCategory: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.isTrace.isVisible = sharePrefUtils.getString(Api.USER_TYPE).equals("TO")

        startRouteObserve()
        startOutletsObserve()
        startSocketGroupUsersObserve()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            binding.spinnerLayoutSO.visibility = View.VISIBLE
            startSoObserve()
            viewModel.getSoList()
        } else {
            binding.spinnerLayoutSO.visibility = View.GONE
        }

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.getRoutes(soNewList[position].id.toString())
                selectedSo = soNewList[position]

                if (binding.isTrace.isChecked) {
                    sharePrefUtils.getString(TRACE_GROUP_ID)
                        ?.let { viewModel.getSocketUserByGroup("65c9e5ff8c5c69794c6f3683") }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerRoutes.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                routeID = routeNewList[position].id.toString()

                if (!binding.isTrace.isChecked) {
                    if (binding.isVerified.isChecked) {
                        viewModel.getOutletList(
                            routeNewList[position].id.toString(),
                            "1",
                            selectedCategory
                        )
                    } else {
                        viewModel.getOutletList(
                            routeNewList[position].id.toString(),
                            "0",
                            selectedCategory
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        categoryList = arrayListOf("All", "A", "B", "C", "D", "E", "F", "P", "MP", "WS")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, categoryList
        )
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                if (routeID.isEmpty()) {
                    return
                }
                if (binding.isTrace.isChecked) {
                    return
                }

                if (position == 0) {
                    selectedCategory = ""
                    if (binding.isVerified.isChecked) {
                        viewModel.getOutletList(routeID, "1", selectedCategory)
                    } else {
                        viewModel.getOutletList(routeID, "0", selectedCategory)
                    }
                } else {
                    selectedCategory = categoryList[position]
                    if (binding.isVerified.isChecked) {
                        viewModel.getOutletList(routeID, "1", selectedCategory)
                    } else {
                        viewModel.getOutletList(routeID, "0", selectedCategory)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.isTrace.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                sharePrefUtils.getString(TRACE_GROUP_ID)
                    ?.let { viewModel.getSocketUserByGroup("65c9e5ff8c5c69794c6f3683") }
                subscribeSocket()
            } else {
                SocketHandler.closeConnection()
                if (routeID.isNotEmpty()) {
                    viewModel.getOutletList(
                        routeID,
                        if (binding.isVerified.isChecked) "1" else "0",
                        selectedCategory
                    )
                }
            }
        }


        binding.isVerified.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (routeID.isEmpty()) {
                return@setOnCheckedChangeListener
            }
            if (binding.isTrace.isChecked) {
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                viewModel.getOutletList(routeID, "1", selectedCategory)
            } else {
                viewModel.getOutletList(routeID, "0", selectedCategory)
            }
        }


    }

    private fun startSoObserve() {
        lifecycleScope.launch {
            viewModel.soResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSoObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSoObserve: Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSoObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSoObserve::Success ${it.data}")

                        AppLogger.log("SIZE: ${it.data?.soList?.get(0)?.salesOfficers?.size}")

                        soNewList = it.data?.soList?.get(0)?.salesOfficers ?: emptyList()
                        val soNameList =
                            it.data?.soList?.get(0)?.salesOfficers?.map { so -> so.userName }

                        if (!soNameList.isNullOrEmpty()) {
                            AppLogger.log("SIZE: ${soNameList.size}")
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item, soNameList
                            )
                            binding.spinnerSO.adapter = adapter
                        } else {
                            Toast.makeText(requireContext(), "So is empty", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    private fun startRouteObserve() {
        lifecycleScope.launch {
            viewModel.routeResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startRouteObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startRouteObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startRouteObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startRouteObserve:: Success ${it.data?.routes?.size}")

                        routeNewList = it.data?.routes ?: emptyList()

                        val routeNames = it.data?.routes?.map { route -> route.routeName }

                        if (routeNewList.isNotEmpty()) {
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                routeNames ?: emptyList<String>()
                            )
                            binding.spinnerRoutes.adapter = adapter

                        } else {
                            val adapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                routeNames ?: emptyList<String>()
                            )
                            binding.spinnerRoutes.adapter = adapter
                            Toast.makeText(requireContext(), "Route is empty.", Toast.LENGTH_SHORT)
                                .show()

                            routeID = ""
                            mMap.clear()
                            binding.shopCount.text = getString(R.string.outlet_s, "0")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun startOutletsObserve() {
        lifecycleScope.launch {
            viewModel.outletResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startOutletsObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startOutletsObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startOutletsObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startOutletsObserve:: Success ${it.data?.outlets?.size}")

                        mMap.clear()

                        if (it.data?.outlets?.isEmpty() == true) {
                            binding.shopCount.text = getString(R.string.outlet_s, 0)
                            Toast.makeText(requireContext(), "Outlets empty.", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            binding.shopCount.text =
                                getString(R.string.outlet_s, it.data?.outlets!!.size)
                            it.data.outlets.forEach { outlet ->
                                plotMarker(outlet, getMarkerIcon())
                            }
                        }

                    }
                }
            }
        }
    }

    private fun startSocketGroupsObserve() {
        lifecycleScope.launch {
            viewModel.socketGroupResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSocketGroupsObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSocketGroupsObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSocketGroupsObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startSocketGroupsObserve:: Success ${it.data?.groups}")

                    }
                }
            }
        }
    }

    private fun startSocketGroupUsersObserve() {
        lifecycleScope.launch {
            viewModel.socketUsersResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startSocketGroupUsersObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startSocketGroupUsersObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startSocketGroupUsersObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log(
                            "startSocketGroupUsersObserve:: Success ${it.data?.data?.groupUsers}"
                        )
                        socketLocalUserList = it.data?.data?.groupUsers ?: emptyList()

                        it.data?.data?.groupUsers?.forEach { localUser ->
                            AppLogger.log("LOCAL USER:: $localUser")
                            if (selectedSo?.phone == localUser.phone) {
                                AppLogger.log("MARKER TRACE:: True")
                                plotTraceUser(
                                    selectedSo?.userName!!,
                                    localUser.positionUpdatedAt.convertDate()!!,
                                    localUser.userLastLat,
                                    localUser.userLastLon,
                                    getLiveMarkerIcon(1)
                                )
                            } else {
                                toast("User not found.")
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initSocket() {

    }

    private fun subscribeSocket() {
        SocketHandler.setSocket(sharePrefUtils.getString(Api.TRACE_TOKEN)!!)
        SocketHandler.establishConnection()
        val socketHandler = SocketHandler.getSocket()

        AppLogger.log("socketHandler::ID:: ${socketHandler.id()}")

        socketHandler.emit("joinGroup", "group_65c9e5ff8c5c69794c6f3683")
        socketHandler.on("gpx") { arg ->
            AppLogger.log("socketHandler:: ${arg[0]}")
            val socketResponse = Gson().fromJson(arg[0].toString(), SocketResponse::class.java)
            AppLogger.log("socketHandler:: $socketResponse")

            requireActivity().runOnUiThread {
                if (selectedSo?.phone == socketResponse.phone) {
                    plotTraceUser(
                        selectedSo?.userName!!,
                        socketResponse.updatedAt.convertDate()!!,
                        socketResponse.latitude,
                        socketResponse.longitude,
                        getLiveMarkerIcon(1)
                    )
                } else {
                    toast("User not found.")
                }

            }
        }
    }

    private fun plotTraceUser(
        userName: String,
        time: String,
        lat: Double,
        lon: Double,
        icon: Icon
    ) {
        mMap.clear()
        mMap.addMarker(
            MarkerOptions().position(LatLng(lat, lon))
                .icon(icon)
                .title("$userName | $time")
        )
        val zoom = if (mMap.cameraPosition.zoom > 17.0) mMap.cameraPosition.zoom else 17.0


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom))
    }

    private fun getLiveMarkerIcon(isActive: Int): Icon {
        return if (isActive == 1) {
            IconFactory.getInstance(requireContext())
                .fromResource(R.drawable.trace_active)
        } else {
            IconFactory.getInstance(requireContext())
                .fromResource(R.drawable.trace_inactive)
        }
    }

    private fun getMarkerIcon(): Icon {
        return if (isVerified.isChecked) {
            IconFactory.getInstance(requireContext())
                .fromResource(R.drawable.map_marker_green)
        } else {
            IconFactory.getInstance(requireContext())
                .fromResource(R.drawable.map_marker_red)
        }
    }

    private fun plotMarker(p: Outlet, icon: Icon) {
        val shopName: String = p.outletName
        val m = mMap.addMarker(
            MarkerOptions().position(LatLng(p.latitude.toDouble(), p.longitude.toDouble()))
                .icon(icon)
                .title(p.outletCategory + ", " + shopName)
        )

        if (!binding.isTrace.isChecked)
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        p.latitude.toDouble(),
                        p.longitude.toDouble()
                    ), 12.0
                )
            )
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {
            // Create an instance of LOST location engine
            //initializeLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(Activity())
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(requireContext())) {

            // Get an instance of the component
            val locationComponent = mMap.locationComponent

            // Activate with options
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(requireContext(), loadedMapStyle)
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
                        val lastLocation: Location = result!!.lastLocation!!
                        if (!lastLocation.equals("null")) {
                            //setCameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 17.0);
                        } else {
                            //locationEngine!!.requestLocationUpdates(locationEngineRequest!!, null)
                            showEnableLocationSetting(requireActivity())
                        }
                    }

                    override fun onFailure(exception: java.lang.Exception) {
                        Toast.makeText(
                            requireContext(),
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        } else {
            permissionsManager = PermissionsManager(this as PermissionsListener)
            permissionsManager!!.requestLocationPermissions(requireActivity())
            //showEnableLocationSetting(this@CreateShopActivity)
        }
    }

    private fun showEnableLocationSetting(activity: Activity) {
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
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), zoom!!
            )
        )
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

    override fun onPermissionResult(granted: Boolean) {
        mMap.getStyle { style ->
            if (granted) {
                enableLocationComponent(style)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission not granted",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        AppLogger.log("LIFE CYCLE:: onStart")
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        AppLogger.log("LIFE CYCLE:: onStop")
        super.onStop()
        binding.mapView.onStop()
        SocketHandler.closeConnection()

    }

    override fun onDestroy() {
        AppLogger.log("LIFE CYCLE:: onDestroy")
        super.onDestroy()
        binding.mapView.onDestroy()
        SocketHandler.closeConnection()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onResume() {
        AppLogger.log("LIFE CYCLE:: onResume")
        super.onResume()
        binding.mapView.onResume()

        if (binding.isTrace.isChecked) {
            runBlocking { initSocket() }
            subscribeSocket()
        }
    }

    override fun onPause() {
        AppLogger.log("LIFE CYCLE:: onPause")
        super.onPause()
        SocketHandler.closeConnection()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mMap = mapboxMap
        mMap.setStyle(
            Style.Builder().fromUrl(getString(R.string.map_view_styleUrl))
        ) { style: Style? -> enableLocationComponent(style!!) }

        val uiSettings: UiSettings = mapboxMap.uiSettings
        uiSettings.isCompassEnabled = false
        mMap.setMaxZoomPreference(25.5)

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO")) {
            AppLogger.log("MAP READY ROUTE CALL")
            sharePrefUtils.getString(Api.USER_ID)?.let { viewModel.getRoutes(it) }
        }

        binding.fab.setOnClickListener {
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
        }

        //setupWebSocket()
    }
}