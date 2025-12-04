package com.barikoi.cnlapp.ui.navigation


import android.os.Bundle
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.utils.extension.toast
import kotlinx.coroutines.DelicateCoroutinesApi
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncher
import org.maplibre.navigation.android.navigation.ui.v5.NavigationLauncherOptions
import org.maplibre.navigation.android.navigation.ui.v5.route.NavigationRoute
import org.maplibre.navigation.android.navigation.v5.models.DirectionsCriteria
import org.maplibre.navigation.android.navigation.v5.models.DirectionsResponse
import org.maplibre.navigation.android.navigation.v5.models.DirectionsRoute
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NavigationActivity : BaseActivity() {
    private var isNavigationActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val originLat = intent.getDoubleExtra("origin_lat", 0.0)
        val originLng = intent.getDoubleExtra("origin_lng", 0.0)
        val destinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        val destinationLng = intent.getDoubleExtra("destination_lng", 0.0)

        startNavigation(originLat, originLng, destinationLat, destinationLng)
    }

    /**
     * Initiates navigation using the provided origin and destination coordinates.
     *
     * @param originLat Latitude of the origin point.
     * @param originLng Longitude of the origin point.
     * @param destinationLat Latitude of the destination point.
     * @param destinationLng Longitude of the destination point.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun startNavigation(
        originLat: Double,
        originLng: Double,
        destinationLat: Double,
        destinationLng: Double,
    ) {
        val destinationPosition = Point.fromLngLat(destinationLng, destinationLat)
        val originPosition = Point.fromLngLat(originLng, originLat)

        try {
            getNavigation(originPosition, destinationPosition) { route ->
                if (route == null) {
                    toast("No route found")
                    return@getNavigation
                }

                val options = NavigationLauncherOptions.builder()
                    .shouldSimulateRoute(false)
                    .directionsRoute(route)
                    .initialMapCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(originPosition.latitude(), originPosition.longitude()))
                            .zoom(16.0)
                            .bearing(2.0)
                            .build()
                    )
                    .build()
                try {
                    NavigationLauncher.startNavigation(this, options)
                    toast("Navigation started successfully.")
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("Failed to start navigation: ${e.message}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast("Error during setup: ${e.message}")
        }
    }

    /**
     * Fetches navigation route from origin to destination.
     *
     * @param origin The starting point of the route.
     * @param destination The ending point of the route.
     * @param onRouteReady Callback function to handle the route once it's ready.
     */
    private fun getNavigation(
        origin: Point?,
        destination: Point?,
        onRouteReady: (DirectionsRoute?) -> Unit
    ) {
        try {
            val navRoute = NavigationRoute.builder(this)
                .accessToken("pk.test")
                .baseUrl("https://api.admin.barikoi.com/api/v2/navigate/")
                .user("gh")
                .annotations("")
                .profile("driving-traffic")
                .origin(origin!!)
                .destination(destination!!)
                .alternatives(true)
                .voiceUnits(DirectionsCriteria.METRIC)
                .build()

            navRoute.getRoute(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if (response.isSuccessful) {
                        val directionsResponse = response.body()
                        if (directionsResponse == null || directionsResponse.routes().isEmpty()) {
                            onRouteReady(null) // No routes available
                        } else {
                            onRouteReady(
                                directionsResponse.routes().first()
                            ) // Return the first route
                        }
                    } else {
                        onRouteReady(null) // Response was not successful
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    t.printStackTrace()
                    onRouteReady(null) // Handle failure
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            onRouteReady(null) // Handle exception
        }
    }
}