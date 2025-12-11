package com.barikoi.cnlapp.ui.shop_list

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.callback.LocationFetch
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.data.remote.models.Route
import com.barikoi.cnlapp.data.remote.models.route.NavigationRouteResponse
import com.barikoi.cnlapp.databinding.FragmentShopListBinding
import com.barikoi.cnlapp.ui.adapter.ShopListAdapter
import com.barikoi.cnlapp.ui.create_shop.CreateShopActivity
import com.barikoi.cnlapp.ui.navigation.NavigationActivity
import com.barikoi.cnlapp.ui.route.RouteViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.toHourMinuteString
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.lineCap
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineJoin
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class ShopListFragment : Fragment() {
    private lateinit var binding: FragmentShopListBinding

    private val viewModel: RouteViewModel by activityViewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private var outletList: List<Outlet> = emptyList()

    private var routeListNew: List<Route> = emptyList()
    private var selectedRoute: Route? = null


    private var routesList: List<String> = emptyList()
    private var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()

    private lateinit var adapter: ShopListAdapter

    private var userId: String? = ""

    private var latitudeCurrent: Double = 0.0
    private var longitudeCurrent: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        getLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startRouteObserve()
        startOutletObserve()
        adapter = ShopListAdapter(
            onEditClickListener = {
                onEdit(it)
            },
            onNavigationRouteClick = {
                viewModel.getNavigationRoute(
                    latitudeCurrent.toString(),
                    longitudeCurrent.toString(),
                    it.latitude,
                    it.longitude,
                    "car",
                    true,
                    "geojson"
                )
            },
            onVisitClickListener = {
                onEdit(it)
            },
            sharePrefUtils = sharePrefUtils
        )

        binding.chipFilter.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedIndex = group.indexOfChild(group.findViewById<Chip>(checkedIds[0]))

                when (selectedIndex) {
                    0 -> { // All
                        adapter.updateLocation(latitudeCurrent, longitudeCurrent)
                        updateAdapter(outletList)
                    }

                    1 -> { // Authorize
                        val filteredList = outletList.filter { it.isVerified == 0 }
                        adapter.updateLocation(latitudeCurrent, longitudeCurrent)
                        updateAdapter(filteredList)
                    }


                    2 -> { // Verified
                        val filteredList = outletList.filter { it.isVerified == 1 }
                        adapter.updateLocation(latitudeCurrent, longitudeCurrent)
                        updateAdapter(filteredList)
                    }

                    3 -> { // Rejected
                        val filteredList = outletList.filter { it.isVerified == 2 }
                        adapter.updateLocation(latitudeCurrent, longitudeCurrent)
                        updateAdapter(filteredList)
                    }
                }
            } else {
                // No chip selected — fallback to "All"
                adapter.updateLocation(latitudeCurrent, longitudeCurrent)
                updateAdapter(outletList)
            }
        }

        starNavigationRouteObserve()

        binding.shoplist.layoutManager = LinearLayoutManager(requireContext())
        binding.shoplist.adapter = adapter

        binding.shoplist.adapter = adapter

        viewModel.soSelected.observe(viewLifecycleOwner) {
            userId = it

            viewModel.getRoutes(
                it
            )
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO")) {
            viewModel.getRoutes(
                sharePrefUtils.getString(Api.USER_ID)!!
            )
        }

        binding.refresh.setOnRefreshListener {
            binding.etSearch.setText("")
            viewModel.getOutlets(
                selectedRoute!!.id.toString(),
                if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM") ||
                    sharePrefUtils.getString(Api.USER_TYPE).equals("TO")
                ) {
                    userId!!
                } else {
                    sharePrefUtils.getString(Api.USER_ID)!!
                }
            )

            getLocation()

            binding.refresh.isRefreshing = false
        }

        binding.spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                selectedRoute = routeListNew[position]

                sharePrefUtils.saveString(
                    Api.SELECTED_ROUTE_ID_LIST,
                    routeListNew[position].routeName
                )

                viewModel.getOutlets(
                    selectedRoute!!.id.toString(),
                    if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM") ||
                        sharePrefUtils.getString(Api.USER_TYPE).equals("TO")
                    ) {
                        userId!!
                    } else {
                        sharePrefUtils.getString(Api.USER_ID)!!
                    }
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.chipFilter.chipAll.isChecked = true
                if (s!!.isEmpty()) {
                    adapter.updateLocation(
                        latitudeCurrent,
                        longitudeCurrent
                    )
                    updateAdapter(outletList)
                    return
                }

                adapter.filter.filter(s)

            }

            override fun afterTextChanged(s: Editable?) {}

        })

        binding.createShop.setOnClickListener {
            if (routeListNew.isNotEmpty()) {
                startActivityResult.launch(
                    Intent(
                        requireActivity(),
                        CreateShopActivity::class.java
                    ).putExtra("requestCode", 55)
                        .putParcelableArrayListExtra(ROUTE_LIST, ArrayList(routeListNew))

                        .putStringArrayListExtra("routes", ArrayList(routesList))
                        .putExtra("routeList", routeNameList)
                )
            } else {
                Toast.makeText(requireContext(), "Routes not Available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val ROUTE_LIST = "ROUTE_LIST"
    }


    private fun startRouteObserve() {
        lifecycleScope.launch {
            viewModel.routeResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startRouteObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startRouteObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startRouteObserve::Loading")
                        binding.progressBar2.isVisible = true
                    }

                    is ApiState.Success -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startRouteObserve:: Success ${it.data?.routes}")

                        routeListNew = it.data?.routes ?: emptyList()

                        val routeName = routeListNew.map { route -> route.routeName }
                        routesList = routeListNew.map { route -> route.routeName }


                        routeNameList = ArrayList(routeListNew.map { a-> Pair(a.id.toString(), a.routeName) })


                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            routeName
                        )
                        binding.spinnerRoutes.adapter = adapter

                        binding.createShop.isVisible = true
                    }
                }
            }
        }
    }

    private fun startOutletObserve() {
        lifecycleScope.launch {
            viewModel.outletResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startOutletObserve::Empty")
                    }

                    is ApiState.Error -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startOutletObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startOutletObserve::Loading")
                        binding.progressBar2.isVisible = true
                    }

                    is ApiState.Success -> {
                        binding.progressBar2.isVisible = false
                        AppLogger.log("startOutletObserve:: Success ${it.data?.outlets}")
                        outletList = it.data?.outlets ?: emptyList()
                        adapter.updateLocation(
                            latitudeCurrent,
                            longitudeCurrent
                        )

                        updateAdapter(outletList)


                        val all = outletList.size
                        val authorize = outletList.count { s -> s.isVerified == 0 }
                        val verified = outletList.count { s -> s.isVerified == 1 }
                        val rejected = outletList.count { s -> s.isVerified == 2 }

                        binding.chipFilter.chipAll.text = getString(R.string.all, all.toString())
                        binding.chipFilter.chipAuthorize.text =
                            getString(R.string.authorize, authorize.toString())
                        binding.chipFilter.chipRejected.text =
                            getString(R.string.rejected, rejected.toString())
                        binding.chipFilter.chipVerified.text =
                            getString(R.string.verified_, verified.toString())

                        binding.chipFilter.chipAll.isChecked = true
                    }
                }
            }
        }
    }

    private fun updateAdapter(outletList: List<Outlet>) {
        val mappedWithDistance = outletList.map {
            val distance = ViewUtils.getDistance(
                latitudeCurrent,
                longitudeCurrent,
                it.latitude.toDouble(),
                it.longitude.toDouble()
            )
            it.copy(distance = distance)
        }.sortedBy { it.distance }
        adapter.updateShopList(mappedWithDistance)

    }

    private fun starNavigationRouteObserve() {
        lifecycleScope.launch {
            viewModel.navigationRouteResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("starNavigationRouteObserve::Empty")
                        binding.progressBar2.isVisible = false
                    }

                    is ApiState.Error -> {
                        AppLogger.log("starNavigationRouteObserve::Error ${it.error}")
                        binding.progressBar2.isVisible = false

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("starNavigationRouteObserve::Loading")
                        binding.progressBar2.isVisible = true
                    }

                    is ApiState.Success -> {
                        AppLogger.log("starNavigationRouteObserve:: Success ${it.data}")
                        binding.progressBar2.isVisible = false

                        showRouteDialog(it.data!!)
                    }
                }
            }
        }
    }

    fun showRouteDialog(routeResponse: NavigationRouteResponse) {
        val dialog = Dialog(requireContext())
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_navigation_route_view)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val btnSubmit = dialog.findViewById<MaterialButton>(R.id.btnNavigate)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val tvDistance = dialog.findViewById<TextView>(R.id.tvDistance)
        val tvDuration = dialog.findViewById<TextView>(R.id.tvDuration)

        tvTitle.text =
            getString(
                R.string.route_distance,
                routeResponse.waypoints.first().name,
                routeResponse.waypoints.last().name
            )

        tvDistance.text = getString(
            R.string.distance_km,
            String.format(
                Locale.ENGLISH, "%.2f",
                routeResponse.routes[0].distance / 1000
            )
        )

        tvDuration.text = routeResponse.routes[0].duration.toHourMinuteString()

        val mapView: MapView = dialog.findViewById(R.id.mapView)

        mapView.getMapAsync { map: MapLibreMap ->
            map.uiSettings.apply {
                isAttributionEnabled = false
                isLogoEnabled = false
                isCompassEnabled = false
            }

            map.setStyle(getString(R.string.map_view_styleUrl)) { style ->

                if (routeResponse.routes.isEmpty()) {
                    return@setStyle
                }

                val route = routeResponse.routes[0]
                val geometry = route.geometry

                // Parse the GeoJSON line string
                val lineString = LineString.fromJson(Gson().toJson(geometry)!!)
                val routeFeature = Feature.fromGeometry(lineString)
                val featureCollection = FeatureCollection.fromFeatures(listOf(routeFeature))

                val sourceId = "route-source"
                val layerId = "route-layer"

                style.addSource(GeoJsonSource(sourceId, featureCollection))
                style.addLayer(
                    LineLayer(layerId, sourceId)
                        .withProperties(
                            lineColor(Color.BLUE),
                            lineWidth(5f),
                            lineCap("round"),
                            lineJoin("round")
                        )
                )

                // Fit camera to route bounds
                val points = lineString.coordinates()
                val boundsBuilder = LatLngBounds.Builder()
                for (p in points) boundsBuilder.include(LatLng(p.latitude(), p.longitude()))

                map.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        100
                    )
                )

                // Optionally add start & end markers
                val waypoints = routeResponse.waypoints
                if (waypoints.size >= 2) {
                    val start = waypoints.first().location
                    val end = waypoints.last().location

                    map.addMarker(
                        org.maplibre.android.annotations.MarkerOptions()
                            .position(LatLng(start[1], start[0]))
                            .title("Start")
                    )
                    map.addMarker(
                        org.maplibre.android.annotations.MarkerOptions()
                            .position(LatLng(end[1], end[0]))
                            .title("Destination")
                    )
                }
            }
        }

        mapView.onStart()
        dialog.setOnDismissListener {
            mapView.onStop()
            mapView.onDestroy()
        }

        btnSubmit.setOnClickListener {
            dialog.dismiss()

            startActivity(
                Intent(requireActivity(), NavigationActivity::class.java)
                    .putExtra("origin_lat", routeResponse.waypoints.first().location[1]) // latitude
                    .putExtra(
                        "origin_lng",
                        routeResponse.waypoints.first().location[0]
                    ) // longitude
                    .putExtra(
                        "destination_lat",
                        routeResponse.waypoints.last().location[1]
                    ) // latitude
                    .putExtra(
                        "destination_lng",
                        routeResponse.waypoints.last().location[0]
                    ) // longitude
            )
        }
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

    private var startActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == 55) {
            if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM") ||
                sharePrefUtils.getString(Api.USER_TYPE).equals("TO")
            ) {
                viewModel.getOutlets(selectedRoute!!.id.toString(), userId!!)
            } else {
                viewModel.getOutlets(
                    selectedRoute!!.id.toString(),
                    sharePrefUtils.getString(Api.USER_ID)!!
                )
            }
        }
    }

    private fun getLocation() {
        ViewUtils.getLocation(requireContext(), requireActivity(), object : LocationFetch {
            override fun onFetchSuccess(location: Location) {
                latitudeCurrent = location.latitude
                longitudeCurrent = location.longitude
            }

            override fun onFailure() {
                toast("Could not get location")
            }
        })
    }

    private fun onEdit(shops: Outlet) {
        if (routeListNew.isNotEmpty()) {
            startActivityResult.launch(
                Intent(requireActivity(), CreateShopActivity::class.java)
                    .putExtra("requestCode", 55)
                    .putExtra("fromEdit", shops)
                    .putParcelableArrayListExtra(ROUTE_LIST, ArrayList(routeListNew))
                    .putStringArrayListExtra("routes", ArrayList(routesList))
                    .putExtra("routeList", routeNameList)
            )
        } else {
            Toast.makeText(requireContext(), "Routes not Available", Toast.LENGTH_SHORT).show()
        }
    }
}