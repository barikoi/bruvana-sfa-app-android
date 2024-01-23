package com.barikoi.cnlapp.Attendance.Fragment

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.barikoitrace.TraceMode
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Attendance.AttendanceFragment.Companion.viewPager2
import com.barikoi.cnlapp.Attendance.AttendanceFragment.Companion.viewpagertab2
import com.barikoi.cnlapp.Attendance.Fragment.SO.HistoryFragment
import com.barikoi.cnlapp.Attendance.Fragment.SO.SummaryFragment
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.*
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.callback.LocationFetch
import com.barikoi.cnlapp.imagecapture.RoomDb.ImageDatabase
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_create_shop.*
import kotlinx.android.synthetic.main.fragment_create_attendance.*
import kotlinx.android.synthetic.main.fragment_create_attendance.imagepicker
import kotlinx.android.synthetic.main.fragment_create_attendance.spinnerLayout
import kotlinx.android.synthetic.main.fragment_create_attendance.spinnerRoutes
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class CreateAttendanceFragment : Fragment() {

    private val _sdfWatchTime = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    private val _sdfWatchDay = SimpleDateFormat("EEEE", Locale.ENGLISH)
    private val _sdfWatchDate = SimpleDateFormat("MMMM dd", Locale.ENGLISH)
    private val _sdfWatchYear = SimpleDateFormat("yyyy", Locale.ENGLISH)
    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id : String? = null
    var token : String? = null
    var user_type : String? = null
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
        imagepicker.setCameraLauncher(startCamera)
        getImageFromDB()

        if (user_type.equals("TO", true)){
            spinnerLayout.visibility = View.GONE
            titleRoute.visibility = View.GONE
        }else{
            spinnerLayout.visibility = View.VISIBLE
            titleRoute.visibility = View.VISIBLE
            getAllRoutes(Api.routes_withfilter+"?with_geometry=0&user_id="+user_id)

            spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 > 0) {
                        val view1: TextView =
                            p0!!.getChildAt(0) as TextView
                        view1.setTextColor(mContext!!.resources.getColor(R.color.black))
                        if (routeNameList!![p2].first.length > 0) {
                            route_id = routeNameList!![p2].first.toInt()
                            selectedRoute = routeNameList!![p2].second
                        }else{
                            route_id = null
                            selectedRoute = ""
                        }
                    } else {
                        val view1: TextView =
                            p0!!.getChildAt(0) as TextView
                        view1.setTextColor(mContext!!.resources.getColor(R.color.text_title_2))
                        route_id = null
                        selectedRoute = ""
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        }

        getLocation("reversegeo")

        checkAttendance()

        imgRefresh.setOnClickListener {
            rotateAnimation(imgRefresh, 0f, 380f)
            getLocation("reversegeo")
        }
        btnCheckIn.setOnClickListener {
            if (user_type.equals("TO", true)){
                if (isImageAdded){
                    progressBar.visibility = View.VISIBLE
                    getLocation("check_in")
                }else{
                    if (!isImageAdded){
                        Toast.makeText(mContext, "Upload image for attendance", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                if (selectedRoute.length > 0 && isImageAdded){
                    progressBar.visibility = View.VISIBLE
                    getLocation("check_in")
                }else{
                    if (!isImageAdded){
                        Toast.makeText(mContext, "Upload image for attendance", Toast.LENGTH_SHORT).show()
                    }
                    if (selectedRoute.length == 0){
                        Toast.makeText(mContext, "Select route for check in", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        btnCheckOut.setOnClickListener {
            if (isImageAdded){
                progressBar.visibility = View.VISIBLE
                getLocation("check_out")
            }else{
                Toast.makeText(mContext, "Upload image for check out", Toast.LENGTH_SHORT).show()
            }
        }

        btnCheckedAlready.setOnClickListener {
            checkAttendance()
        }
    }

    private fun checkAttendance() {
        ApiServices.apiGET(
            Api.check_today_attendance,
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try{
                        val data = JSONObject(response)
                        if (data.has("attendances") && !data.isNull("attendances")){
                            val attendanceObj = data.getJSONObject("attendances")
                            val check_in = attendanceObj.getString("checkin_time")
                            val check_out = attendanceObj.getString("checkout_time")
                            if (!check_in.equals("null") && !check_out.equals("null")){
                                btnCheckIn.visibility = View.GONE
                                btnCheckOut.visibility = View.GONE
                                btnCheckedAlready.visibility = View.VISIBLE
                            }else if (!check_in.equals("null") && check_out.equals("null")){
                                btnCheckIn.visibility = View.GONE
                                btnCheckOut.visibility = View.VISIBLE
                                btnCheckedAlready.visibility = View.GONE
                            }else{
                                btnCheckIn.visibility = View.VISIBLE
                                btnCheckOut.visibility = View.GONE
                                btnCheckedAlready.visibility = View.GONE
                            }
                        }else{
                            btnCheckIn.visibility = View.VISIBLE
                            btnCheckOut.visibility = View.GONE
                            btnCheckedAlready.visibility = View.GONE
                        }

                    }catch (e:Exception){
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
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
                    Toast.makeText(mContext!!, e.message, Toast.LENGTH_SHORT).show()
                }

            })
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

    private fun getImageFromDB() {
        var imageList: java.util.ArrayList<Images?>? = java.util.ArrayList()
        imageList = appDatabase!!.imagesDao()!!.getAllImageDB("Attendance") as java.util.ArrayList<Images?>?
        if (imageList!!.size > 0){
            for(p in 0 until imageList.size){
                val dbPhotoPath = imageList[p]!!.filePath
                val fileExist : Boolean = File(dbPhotoPath).canRead()
                if(fileExist) {
                    try {
                        var bitmap = imagepicker.getRotateImage(dbPhotoPath)
                        isImageAdded = true
                        imagepicker.setLocalImage(bitmap, dbPhotoPath, imageList[p]!!.position, "", "Attendance")
                    } catch (e:Exception ) {
                        e.printStackTrace()
                    }
                    Log.d("Imagepos", "ImageList Pos: " + imageList[p]!!.position + "p: " +(p + 1))
                    if (imageList[p]!!.position != p + 1) {
                        Executors.newSingleThreadExecutor().execute {
                            appDatabase!!.imagesDao()!!.updatePosition(dbPhotoPath, p + 1, "Attendance")
                        }
                    }
                }else{
                    Executors.newSingleThreadExecutor()
                        .execute { appDatabase!!.imagesDao()!!.deleteImage(p+1, "Attendance") }
                }


            }
        }
    }
    fun checkInAttendance(location: Location){
        val byteparams: MutableMap<String, VolleyMultipartRequest.DataPart> = java.util.HashMap()
        var imagesList = ArrayList<Images>()
        imagesList = appDatabase!!.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images>
        if (imagesList.size > 0) {
            val fileExist = File(imagesList[0].filePath).canRead()
            if (fileExist) {
                val imagename = imagesList[0].filePath.substring(
                    imagesList[0].filePath.lastIndexOf("/")
                )
                byteparams["images[0]"] = VolleyMultipartRequest.DataPart(
                    imagename, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                )
            }
        }
        val params: MutableMap<String, String> = java.util.HashMap()
        params["type"] = "checkin"
        params["latitude"] = location.latitude.toString()
        params["longitude"] = location.longitude.toString()
        if(route_id != null) params["route_id"] = route_id.toString()
        if(editTextReason.text.toString().length > 0) params["remarks"] = editTextReason.text.toString()

        ApiServices.apiPOSTMultipart(Api.check_in, mQueue!!, token!!, params, byteparams, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                progressBar.visibility = View.GONE
                val data = JSONObject(String(response.data))
                val message = data.getString("message")
                if (prefs!!.getString(Api.USER_TYPE, "").equals("SO", true)) {
                    ViewUtils.startTracking(ACTIVITY, mContext!!)
                }
                appDatabase!!.imagesDao()!!.deleteAllImages()
                ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                    override fun onConfirmed() {
                        editor!!.putString(Api.SELECTED_ROUTE_ID, route_id.toString())
                        editor!!.putString(Api.SELECTED_ROUTE_NAME, selectedRoute)
                        editor!!.commit()

                        val titles = arrayOf(mContext!!.resources.getString(R.string.attendance), mContext!!.resources.getString(R.string.history), mContext!!.resources.getString(R.string.summary))
                        val fragments = ArrayList<Fragment>()
                        fragments.add(CreateAttendanceFragment())
                        fragments.add(HistoryFragment())
                        fragments.add(SummaryFragment())
                        viewPager2!!.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
                        TabLayoutMediator(viewpagertab2!!, viewPager2!!,
                            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                                tab.text = titles[position]
                                tab.parent
                            }).attach()
                        viewPager2!!.setCurrentItem(0);
                        viewPager2!!.setUserInputEnabled(false)
                        for (i in 0 until viewpagertab2!!.getTabCount()) {
                            val tab = (viewpagertab2!!.getChildAt(0) as ViewGroup).getChildAt(i)
                            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                            p.setMargins(15, 15, 10, 15)
                            tab.requestLayout()
                        }
                        //CreateOrderFragment.setCurrentFragment(CreateAttendanceFragment(), ACTIVITY)
                    }

                    override fun onCanceled() {
                        TODO("Not yet implemented")
                    }

                })
            }

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onResponseFailure(error: VolleyError) {
                progressBar.visibility = View.GONE
                val s = String(
                    error.networkResponse.data,
                    StandardCharsets.UTF_8
                )
                val data = JSONObject(s)
                val message = data.getString("message")
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }

            override fun onException(e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun checkOutAttendance(location: Location) {
        val byteparams: MutableMap<String, VolleyMultipartRequest.DataPart> = java.util.HashMap()
        var imagesList = ArrayList<Images>()
        imagesList = appDatabase!!.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images>
        if (imagesList.size > 0) {
            val fileExist = File(imagesList[0].filePath).canRead()
            if (fileExist) {
                val imagename = imagesList[0].filePath.substring(
                    imagesList[0].filePath.lastIndexOf("/")
                )
                byteparams["images[0]"] = VolleyMultipartRequest.DataPart(
                    imagename, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                )
            }
        }
        val params: MutableMap<String, String> = java.util.HashMap()
        params["latitude"] = location.latitude.toString()
        params["longitude"] = location.longitude.toString()

        ApiServices.apiPOSTMultipart(Api.check_out, mQueue!!, token!!, params, byteparams, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                progressBar.visibility = View.GONE
                val data = JSONObject(String(response.data))
                val message = data.getString("message")
                if (prefs!!.getString(Api.USER_TYPE, "").equals("SO", true)) {
                    BarikoiTrace.stopTracking()
                }
                appDatabase!!.imagesDao()!!.deleteAllImages()
                ViewUtils.viewDialogResponse(mContext!!, message, object : DialogListener {
                    override fun onConfirmed() {
                        editor!!.putString(Api.SELECTED_ROUTE_ID, route_id.toString())
                        editor!!.putString(Api.SELECTED_ROUTE_NAME, selectedRoute)
                        editor!!.commit()
                        checkAttendance()

                        val titles = arrayOf(mContext!!.resources.getString(R.string.attendance), mContext!!.resources.getString(R.string.history), mContext!!.resources.getString(R.string.summary))
                        val fragments = ArrayList<Fragment>()
                        fragments.add(CreateAttendanceFragment())
                        fragments.add(HistoryFragment())
                        fragments.add(SummaryFragment())
                        viewPager2!!.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
                        TabLayoutMediator(viewpagertab2!!, viewPager2!!,
                            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                                tab.text = titles[position]
                                tab.parent
                            }).attach()
                        viewPager2!!.setCurrentItem(0);
                        viewPager2!!.setUserInputEnabled(false)
                        for (i in 0 until viewpagertab2!!.getTabCount()) {
                            val tab = (viewpagertab2!!.getChildAt(0) as ViewGroup).getChildAt(i)
                            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
                            p.setMargins(15, 15, 10, 15)
                            tab.requestLayout()
                        }
                    }

                    override fun onCanceled() {
                        TODO("Not yet implemented")
                    }

                })
            }

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onResponseFailure(error: VolleyError) {
                progressBar.visibility = View.GONE
                val s = String(
                    error.networkResponse.data,
                    StandardCharsets.UTF_8
                )
                val data = JSONObject(s)
                val message = data.getString("message")
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
            }

            override fun onException(e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun getLocation(choice: String){
        ViewUtils.getLocation(mContext!!, ACTIVITY, object : LocationFetch{
            override fun onFetchSuccess(location: Location) {
                if (choice.equals("check_in")){
                    checkInAttendance(location)
                }else if(choice.equals("check_out")){
                    checkOutAttendance(location)
                }else if(choice.equals("reversegeo")){
                    reverseGeoAddress(mContext!!, location.latitude, location.longitude)
                }
            }
            override fun onFailure() {

            }

        })
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
                            routeNameList!!.add(Pair("", mContext!!.resources.getString(R.string.select_route)))
                            routesList.add(mContext!!.resources.getString(R.string.select_route))
                            for(i in 0 until routesArray.length()){
                                val routeObj = routesArray.getJSONObject(i)

                                routeNameList!!.add(Pair(routeObj.getString("id"), routeObj.getString("route_name")))
                                routesList.add(routeObj.getString("route_name"))
                            }
                            if (spinnerRoutes != null) {
                                if (spinnerRoutes.adapter == null){
                                    val adapter = object : ArrayAdapter<String>(
                                        mContext!!,
                                        android.R.layout.simple_spinner_item, routesList
                                    ) {
                                        override fun isEnabled(position: Int): Boolean {
                                            return position != 0
                                        }

                                        override fun getDropDownView(
                                            position: Int,
                                            convertView: View?,
                                            parent: ViewGroup
                                        ): View {
                                            val view: TextView = super.getDropDownView(
                                                position,
                                                convertView,
                                                parent
                                            ) as TextView
                                            //set the color of first item in the drop down list to gray
                                            if (position == 0) {
                                                view.setTextColor(mContext!!.resources.getColor(R.color.text_title_2))
                                                view.visibility = View.GONE
                                            } else {
                                                //here it is possible to define color for other items by
                                                //view.setTextColor(Color.RED)
                                                view.setTextColor(resources.getColor(R.color.black))
                                            }
                                            return view
                                        }
                                    }
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
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
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
                        var address = ""
                        if (!place.getString("address").equals("null")) {
                            address = place.getString("address")
                        }
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

    var startCamera = registerForActivityResult(
        StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            val filePath = prefs!!.getString(ApiCall.IMAGE_PATH, "")
            if (result.getResultCode() == RESULT_CANCELED) {
                if (filePath != null) {
                    Log.d("Image", "Canceled: $filePath")
                    imagepicker.deleteFileLocal(filePath)
                    editor!!.putString(ApiCall.IMAGE_PATH, "")
                    editor!!.apply()
                }
            }
            if (result.getResultCode() == RESULT_OK) {
                Log.e("imageUtils", "OnActivity result code 1: " + Activity.RESULT_OK)
                var imagePosition = 0
                var imageList: ArrayList<Images?>? = ArrayList()
                imageList = appDatabase!!.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images?>?
                Log.d("Imagepos", "List: $imageList")
                imagePosition = if (imageList!!.size > 0) {
                    imageList[imageList.size - 1]!!.position + 1
                } else {
                    imagePosition + 1
                }
                imagepicker.AddNewImage(result.data, CAMERA, imagePosition, "Attendance", prefs!!.getString(ApiCall.IMAGE_PATH, "")!!)
                try {
                    val placeImage = Images(
                        null, imagePosition,
                        prefs!!.getString(ApiCall.IMAGE_PATH, "")!!, "Attendance"
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
        })

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        user_id = prefs!!.getString(com.barikoi.cnlapp.Utils.Api.USER_ID, "")
        token = prefs!!.getString(Api.TOKEN, "")
        user_type = prefs!!.getString(Api.USER_TYPE, "")
        ACTIVITY = context as MainActivity
        appDatabase = ImageDatabase.getInstance(mContext!!)
    }
}