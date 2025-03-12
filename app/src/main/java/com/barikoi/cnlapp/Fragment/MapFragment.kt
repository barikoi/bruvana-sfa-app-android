@file:Suppress("DEPRECATION", "UNUSED_VARIABLE")

package com.barikoi.cnlapp.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import com.barikoi.cnlapp.data.remote.models.GroupUser
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.data.remote.models.SoUser
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.FragmentMapBinding
import com.barikoi.cnlapp.socket.SocketHandler
import com.barikoi.cnlapp.socket.model.SocketResponse
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.Api.TRACE_GROUP_ID
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.convertDate
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.getDifferenceInMinutes
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.gson.Gson
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.engine.LocationEngine
import com.mapbox.mapboxsdk.location.engine.LocationEngineCallback
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.location.engine.LocationEngineResult
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback, PermissionsListener {
    private lateinit var binding: FragmentMapBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private val viewModel: MapViewModel by viewModels()

    private lateinit var mMap: MapboxMap

    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null

    private var permissionsManager: PermissionsManager? = null

    private var toList: List<To> = emptyList()
    private var soNewList: List<SoUser> = emptyList()

    private var selectedSo: SoUser? = null

    private var routeNewList: List<Route> = emptyList()
    private var routeID = ""

    private var socketLocalUserList: List<GroupUser> = emptyList()

    private var categoryList: ArrayList<String> = ArrayList()
    private var selectedCategory: String = ""

    private lateinit var loading: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater, container, false)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.isTrace.isVisible = sharePrefUtils.getString(Api.USER_TYPE).equals("TO")
        binding.isTrace.isVisible = sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")

        startRouteObserve()
        startOutletsObserve()
        startSocketGroupUsersObserve()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireContext().loadingDialog {
            loading = it
        }

        SocketHandler.setSocket(sharePrefUtils.getString(Api.TRACE_TOKEN)!!)

        startSoObserve()
        startToObserve()

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            binding.spinnerLayoutSO.visibility = View.VISIBLE
            binding.spinnerLayoutTO.visibility = View.GONE
            viewModel.getSoByTo(
                sharePrefUtils.getString(Api.USER_ID)!!
            )

        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)) {
            binding.spinnerLayoutTO.visibility = View.VISIBLE
            binding.spinnerLayoutSO.visibility = View.VISIBLE
            viewModel.getTo(
                Calendar.getInstance().time.formatDate(),
                Calendar.getInstance().time.formatDate(),
                "0"
            )
        } else {
            binding.spinnerLayoutSO.visibility = View.GONE
            binding.spinnerLayoutTO.visibility = View.GONE
        }

        binding.shopCount.setHapticClickListener {
            val aa = SocketHandler.getSocket()
            AppLogger.log("SocketHandler:: ${aa.id()}")
            AppLogger.log("SocketHandler:: ${aa.connected()}")
            AppLogger.log("SocketHandler:: ${aa.isActive}")
            AppLogger.log("SocketHandler:: ${aa.isActive}")

            SocketHandler.onConnectError()
        }

        binding.spinnerTO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.getSoByTo(
                    toList[position].toId.toString()
                )
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                viewModel.getRoutes(soNewList[position].id.toString())
                selectedSo = soNewList[position]

                if (binding.isTrace.isChecked) {
                    sharePrefUtils.getString(TRACE_GROUP_ID)
                        ?.let { viewModel.getSocketUserByGroup(it) }
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
                sharePrefUtils.getString(TRACE_GROUP_ID)?.let { viewModel.getSocketUserByGroup(it) }
                SocketHandler.establishConnection()
            } else {
                if (SocketHandler.getSocket().isActive)
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

    private fun startToObserve() {
        lifecycleScope.launch {
            viewModel.toResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startToObserve::Empty")
                        loading.hide()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startToObserve::Error ${it.error}")
                        loading.hide()

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startToObserve::Loading")
                        loading.show()
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startToObserve:: Success ${it.data}")
                        loading.hide()

                        if (it.data?.toList.isNullOrEmpty()) {
                            toast("To list empty")
                            return@observe
                        }

                        toList = it.data?.toList ?: emptyList()
                        val toNameList = it.data?.toList?.map { to -> to.toName }!!.toMutableList()

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            toNameList.toMutableList()
                        )
                        binding.spinnerTO.adapter = adapter

                    }
                }
            }
        }
    }

    private fun startSoObserve() {
        lifecycleScope.launch {
            viewModel.soNewResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        loading.hide()
                        AppLogger.log("startSoObserve::Empty")
                    }

                    is ApiState.Error -> {
                        loading.hide()
                        AppLogger.log("startSoObserve: Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        loading.show()
                        AppLogger.log("startSoObserve::Loading")
                    }

                    is ApiState.Success -> {
                        loading.hide()
                        AppLogger.log("startSoObserve::Success ${it.data}")


                        soNewList = it.data?.users ?: emptyList()
                        val soNameList =
                            it.data?.users?.map { so -> so.userName }

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
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startRouteObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startRouteObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar1.isVisible = true
                        AppLogger.log("startRouteObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar1.isVisible = false
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
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startOutletsObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startOutletsObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar1.isVisible = true
                        AppLogger.log("startOutletsObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar1.isVisible = false
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
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startSocketGroupsObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startSocketGroupsObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar1.isVisible = true
                        AppLogger.log("startSocketGroupsObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar1.isVisible = false
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
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startSocketGroupUsersObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar1.isVisible = false
                        AppLogger.log("startSocketGroupUsersObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        binding.progressBar1.isVisible = true
                        AppLogger.log("startSocketGroupUsersObserve::Loading")
                    }

                    is ApiState.Success -> {
                        binding.progressBar1.isVisible = false
                        AppLogger.log(
                            "startSocketGroupUsersObserve:: Success ${
                                it.data?.group
                            }"
                        )

                        subscribeSocketIO()

                        socketLocalUserList = it.data?.group?.groupUsers ?: emptyList()

                        it.data?.group?.groupUsers?.forEach { localUser ->
                            AppLogger.log("LOCAL USER:: $localUser")
                            if (selectedSo?.phone == localUser.phone) {
                                AppLogger.log("MARKER TRACE:: True")
                                AppLogger.log("LOCAL USER MATCH:: $localUser")
                                plotTraceUser(
                                    localUser.name,
                                    if (localUser.positionUpdatedAt != null) localUser.positionUpdatedAt.convertDate()
                                        .toString() else "",
                                    localUser.userLastLat,
                                    localUser.userLastLon,
                                    if (localUser.positionUpdatedAt == null || getDifferenceInMinutes(
                                            localUser.positionUpdatedAt
                                        ) > 10
                                    ) getLiveMarkerIcon(
                                        0
                                    ) else getLiveMarkerIcon(1)

                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subscribeSocketIO() {
        val socketHandler = SocketHandler.getSocket()

        AppLogger.log("socketHandler::ID:: ${socketHandler.id()}")

        socketHandler.emit("joinGroup", "group_${sharePrefUtils.getString(TRACE_GROUP_ID)}")
        socketHandler.on("gpx") { arg ->
            AppLogger.log("socketHandler:: ${arg[0]}")
            val socketResponse = Gson().fromJson(arg[0].toString(), SocketResponse::class.java)
            AppLogger.log("socketHandler:: $socketResponse")

            requireActivity().runOnUiThread {
                AppLogger.log("User")
                if (selectedSo?.phone == socketResponse.phone) {
                    plotTraceUser(
                        selectedSo?.userName!!,
                        socketResponse.updatedAt.convertDate()!!,
                        socketResponse.latitude,
                        socketResponse.longitude,
                        IconFactory.getInstance(requireContext())
                            .fromResource(R.drawable.trace_active)
                    )
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
        return if (binding.isVerified.isChecked) {
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

    }

    override fun onDestroy() {
        AppLogger.log("LIFE CYCLE:: onDestroy")
        super.onDestroy()
        binding.mapView.onDestroy()
        if (SocketHandler.getSocket().isActive)
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
            subscribeSocketIO()
        }
    }

    override fun onPause() {
        AppLogger.log("LIFE CYCLE:: onPause")
        super.onPause()
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

        mapboxMap.setMinZoomPreference(12.0)

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