package com.barikoi.cnlapp.Attendance.Fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.*
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.imagecapture.RoomDb.ImageDatabase
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
import com.google.android.gms.location.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_create_attendance.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class CreateAttendanceFragment : Fragment() {

    private val _sdfWatchTime = SimpleDateFormat("HH:mm")
    private val _sdfWatchDay = SimpleDateFormat("EEEE")
    private val _sdfWatchDate = SimpleDateFormat("MMMM dd")
    private val _sdfWatchYear = SimpleDateFormat("yyyy")
    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id : String? = null
    var token : String? = null
    var appDatabase: ImageDatabase? = null
    private var isImageAdded = false
    private val CAMERA = 4
    var selectedRoute: String = ""
    var route_id: Int? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallback: LocationCallback? = null

    var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_attendance, container, false)
    }

    private fun init() {
        val dateTime: String = _sdfWatchDay.format(Date()) + ", " + _sdfWatchDate.format(Date())+", " + _sdfWatchYear.format(Date())
        tvDate.setText(dateTime)

        imagepicker.taskId = "taskId"
        imagepicker.CAMERA = 4
        imagepicker.setMainactivity(ACTIVITY)
        imagepicker.setFragmetnt(this)

        getAllRoutes(com.barikoi.cnlapp.Utils.Api.routes_withfilter+"?with_geometry=0&sr_id="+user_id)

        spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                route_id = routeNameList!![p2].first.toInt()
                selectedRoute = routeNameList!![p2].second

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        getLocation("reversegeo")
        imgRefresh.setOnClickListener {
            rotateAnimation(imgRefresh, 0f, 380f)
            getLocation("reversegeo")
        }
        btnSubmit.setOnClickListener {
            if (selectedRoute.length > 0 && isImageAdded){
                getLocation("submit")
            }else{
                if (!isImageAdded){
                    Toast.makeText(mContext, "Upload image for attendance", Toast.LENGTH_SHORT).show()
                }
                if (selectedRoute.length == 0){
                    Toast.makeText(mContext, "Select route for attendance", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    fun rotateAnimation(v: View, fromDegrees: Float, toDegrees: Float) {
        // Create an animation instance
        val an: Animation = RotateAnimation(
            fromDegrees, toDegrees, (v.width / 2).toFloat(),
            (v.height / 2).toFloat()
        )
        an.setDuration(500)
        an.setFillAfter(true)
        an.repeatMode = Animation.RESTART
        //v.clearAnimation();
        v.startAnimation(an)
    }

    fun submitAttendance(location: Location){
        val byteparams: MutableMap<String, VolleyMultipartRequest.DataPart> = java.util.HashMap()
        var imagesList = ArrayList<Images>()
        imagesList = appDatabase!!.imagesDao()!!.getAllImageDB() as ArrayList<Images>
        if (imagesList.size > 0) {
            /*for (i in imagesList.indices) {
                val fileExist = File(imagesList[i].filePath).canRead()
                if (fileExist) {
                    val imagename = imagesList[i].filePath.substring(
                        imagesList[i].filePath.lastIndexOf("/")
                    )
                    byteparams["image"] = VolleyMultipartRequest.DataPart(
                        imagename, ImageUtils.decodeFile(imagesList[i].filePath), "image/jpeg"
                    )
                }
            }*/
            val fileExist = File(imagesList[0].filePath).canRead()
            if (fileExist) {
                val imagename = imagesList[0].filePath.substring(
                    imagesList[0].filePath.lastIndexOf("/")
                )
                byteparams["image"] = VolleyMultipartRequest.DataPart(
                    imagename, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                )
            }
        }
        val params: MutableMap<String, String> = java.util.HashMap()
        params["type"] = "checkin"
        params["latitude"] = location.latitude.toString()
        params["longitude"] = location.longitude.toString()
        params["route_id"] = route_id.toString()
        if(editTextReason.text.toString().length > 0) params["late_reason"] = editTextReason.text.toString()

        ApiServices.apiPOSTMultipart(Api.create_attendance, mQueue!!, token!!, params, byteparams, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {

            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                editor!!.putString(Api.SELECTED_ROUTE_ID, route_id.toString())
                editor!!.commit()
                appDatabase!!.imagesDao()!!.deleteAllImages()
                val data = JSONObject(String(response.data))
                val message = data.getString("message")
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onResponseFailure(error: VolleyError) {
                val s = String(
                    error.networkResponse.data,
                    StandardCharsets.UTF_8
                )
                val data = JSONObject(s)
                val message = data.getString("message")
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }

            override fun onException(e: Exception) {
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getLocation(choice: String){
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
                            if (choice.equals("submit")){
                                submitAttendance(location)
                            }else if(choice.equals("reversegeo")){
                                reverseGeoAddress(mContext!!, location.latitude, location.longitude)
                            }

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

    private fun getAllRoutes(url: String) {
        //loading!!.visibility = View.VISIBLE
        routeNameList!!.clear()
        val request = StringRequest(
            Request.Method.GET, url,
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
                            if (spinnerRoutes != null) {
                                if (spinnerRoutes.adapter == null){
                                    val adapter = ArrayAdapter(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routesList
                                    )
                                    spinnerRoutes.adapter = adapter
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
        mQueue!!.add(request)

    }

    fun reverseGeoAddress(context: Context, lat: Double, lng: Double) {
        try {
            val queue = RequestQueueSingleton.getInstance(context.applicationContext).requestQueue
            val request: StringRequest = object : StringRequest(
                Method.GET,
                Api.reverseGeo + "?key=" +Api.APIKEY + "&latitude=" + lat + "&longitude=" + lng,
                Response.Listener { response: String? ->
                    try {
                        val data = JSONObject(response)
                        val place = JSONObject(data.getString("place"))
                        val address = place.getString("address")
                        val city = place.getString("city")
                        val area = place.getString("area")
                        //address[0] = jsonArray.getJSONObject(0).getString("Address");
                        tvLocation.text = address+", "+area+", "+city
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }
                },
                Response.ErrorListener { error: VolleyError ->
                    Sentry.captureException(error)
                    Log.d("MainActivity", "Error: " + error.message)
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["Accept"] = "application/json"
                    return params
                }
            }
            queue.add(request)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val filePath = prefs!!.getString(ApiCall.IMAGE_PATH, "")

        if (resultCode == Activity.RESULT_CANCELED) {
            if (filePath != null) {
                //bottomSheetBehaviorinput.setState(BottomSheetBehavior.STATE_EXPANDED)
                Log.d("Image", "Canceled: $filePath")
                imagepicker.deleteFileLocal(filePath)
                editor!!.putString(ApiCall.IMAGE_PATH, "")
                editor!!.apply()
            }
            return
        }
        if (requestCode == CAMERA) {
            Log.e("imageUtils", "OnActivity result code 1: " + Activity.RESULT_OK)
            var imagePosition = 0
            var imageList: ArrayList<Images?>? = ArrayList()
            imageList = appDatabase!!.imagesDao()!!.getAllImageDB() as ArrayList<Images?>?
            Log.d("Imagepos", "List: $imageList")
            imagePosition = if (imageList!!.size > 0) {
                imageList[imageList.size - 1]!!.position + 1
            } else {
                imagePosition + 1
            }
            imagepicker.AddNewImage(data, CAMERA, imagePosition)
            try {
                val placeImage = Images(
                    null, imagePosition,
                    prefs!!.getString(ApiCall.IMAGE_PATH, "")!!
                )
                isImageAdded = true
                if (imagePosition > 0) {
                    Log.d("Imagepos", "insert")
                    Executors.newSingleThreadExecutor().execute {
                        appDatabase!!.imagesDao()!!.insertAll(placeImage)
                    }
                    editor!!.putString(ApiCall.IMAGE_PATH, "")
                    editor!!.apply()
                }
            } catch (e: java.lang.Exception) {
                Log.e("imageUtils", "OnActivity result 2: $e")
                Sentry.captureException(e)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        user_id = prefs!!.getString(com.barikoi.cnlapp.Utils.Api.USER_ID, "")
        token = prefs!!.getString(Api.TOKEN, "")
        ACTIVITY = context as MainActivity
        appDatabase = ImageDatabase.getInstance(mContext!!)
    }
}