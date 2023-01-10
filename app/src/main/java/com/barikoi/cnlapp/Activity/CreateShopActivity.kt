package com.barikoi.cnlapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.barikoi.cnlapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_create_shop.*

class CreateShopActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private var mapView: MapView? = null
    private var mMap: MapboxMap? = null
    lateinit var fab: FloatingActionButton
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shop)

        val dialog = Dialog(applicationContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_map_view)
        val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
        Mapbox.getInstance(applicationContext, null)
        mapView = dialog.findViewById<MapView>(R.id.mapview)
        mapView!!.onCreate(savedInstanceState)
        mapView!!.getMapAsync(this)

        locationMap.setOnClickListener {

            fab = dialog.findViewById(R.id.fab)

            btnSubmit.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            val window = dialog.window
            window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocation(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(applicationContext)) {

            // Get an instance of the component
            val locationComponent: LocationComponent = mMap!!.getLocationComponent()

            // Activate with options
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(applicationContext, loadedMapStyle).build()
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
                    LocationEngineCallback<LocationEngineResult> {
                    override fun onSuccess(result: LocationEngineResult?) {
                        val lastLocation = result!!.lastLocation
                        if (lastLocation != null) {
                            setCameraPosition(LatLng(lastLocation.latitude, lastLocation.longitude), 15.0)
                        } else {
                            //locationEngine!!.requestLocationUpdates(locationEngineRequest!!, )
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        TODO("Not yet implemented")
                    }

                })


            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(this@CreateShopActivity)
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

    override fun onMapReady(mapboxMap: MapboxMap) {
        mMap = mapboxMap
        mMap!!.setStyle(Style.Builder().fromUrl(getString(R.string.map_view_styleUrl)))


        val uiSettings: UiSettings = mapboxMap!!.uiSettings
        uiSettings.setCompassEnabled(false)

        try {
            mapboxMap.style?.let { it1 ->
                enableLocation(it1)
            }
        }catch (e: Exception){
            e.printStackTrace()
            Sentry.captureException(e)
        }

        fab.setOnClickListener(View.OnClickListener {
            if (locationEngine != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@OnClickListener
                }
                locationEngine!!.getLastLocation(object : LocationEngineCallback<LocationEngineResult>{
                    override fun onSuccess(result: LocationEngineResult?) {
                        val lastLocation = result!!.lastLocation
                        if (lastLocation != null) {
                            setCameraPosition(LatLng(lastLocation.latitude, lastLocation.longitude), 17.0)
                        } else {
                            //locationEngine!!.requestLocationUpdates(locationEngineRequest!!, )
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        TODO("Not yet implemented")
                    }

                })


            } else {
                mapboxMap.style?.let { it1 -> enableLocation(it1) }
            }
        })
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {

    }

    override fun onPermissionResult(granted: Boolean) {
        mMap!!.style?.let { enableLocation(it) }
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
}