package com.barikoi.cnlapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.*
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.imagecapture.RoomDb.ImageDatabase
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
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
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_create_shop.*
import kotlinx.android.synthetic.main.activity_create_shop.imagepicker
import kotlinx.android.synthetic.main.activity_create_shop.spinnerRoutes
import kotlinx.android.synthetic.main.fragment_create_attendance.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class CreateShopActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private var mapView: MapView? = null
    private var mMap: MapboxMap? = null
    lateinit var fab: FloatingActionButton
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var token: String? = null
    var userId: String? = ""
    var srId: String? = ""
    var appDatabase: ImageDatabase? = null
    private var isImageAdded = false
    private val CAMERA = 4

    private var selectedRoute: String? = ""
    private var selectedShopType: String? = ""
    private var selectedCategory: String? = ""
    private var selectedMarketOpportunity: String? = ""
    private var selectedBuyer: Int? = -1

    var routeNameList: java.util.ArrayList<Pair<String, String>>? = ArrayList()
    var routesList : java.util.ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shop)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        appDatabase = ImageDatabase.getInstance(applicationContext)

        val gd = GradientDrawable()
        gd.setColor(resources.getColor(R.color.white))
        gd.cornerRadius = 10f
        gd.setStroke(2, resources.getColor(R.color.required_field))

        spinnerLayoutRoutes.setBackgroundDrawable(gd)
        //imagepickerLayout.setBackgroundDrawable(gd)
        etShopName.setBackgroundDrawable(gd)
        spinnerLayoutType.setBackgroundDrawable(gd)
        spinnerLayoutCategory.setBackgroundDrawable(gd)
        etAddress.setBackgroundDrawable(gd)
        etOwnerName.setBackgroundDrawable(gd)

        imagepicker.taskId = "taskId"
        imagepicker.CAMERA = 4
        imagepicker.setMainactivity(this@CreateShopActivity)
        imagepicker.setCameraLauncher(startCamera)

        routesList = intent.getStringArrayListExtra("routes")
        routeNameList = intent.getParcelableArrayListExtra<Parcelable>("routeList") as java.util.ArrayList<Pair<String, String>>

        if (routesList!!.size > 0){
            routesList!!.add(0, resources.getString(R.string.select_route))
            routeNameList!!.add(0, Pair(
                "",
                resources.getString(R.string.select_route)
            ))
            setRoutes(routesList!!, routeNameList!!)
        }
        //getRoutes()
        getShopType()
        getShopCategory()
        getMarketOpportunity()
        getBuyer()
        getImageFromDB()

        if (intent.hasExtra("fromEdit")){
            tvTitle.text = resources.getString(R.string.update_shop_information)
            isVerified.visibility = View.VISIBLE
            val shops = intent.getSerializableExtra("fromEdit") as Shops
            getShopDetails(shops)
        }else{
            tvTitle.text = resources.getString(R.string.new_shop_creation)
            isVerified.visibility = View.GONE
        }

        btnBack.setOnClickListener {
            setResult(55)
            finish()
            onBackPressed()
        }

        locationMap.setOnClickListener {
            Mapbox.getInstance(applicationContext, null)
            val dialog = Dialog(this)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_map_view)
            val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
            val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

            mapView = dialog.findViewById<MapView>(R.id.mapview)
            mapView!!.onCreate(savedInstanceState)
            mapView!!.getMapAsync(this)
            mapView!!.onStart()

            fab = dialog.findViewById(R.id.fab)

            btnSubmit.setOnClickListener {
                if (mMap!!.cameraPosition.zoom >= 17.0f) {
                    val target = mMap!!.cameraPosition.target
                    latitude = target.latitude
                    longitude = target.longitude
                    var dformat = DecimalFormat("#.#####")
                    if (latitude!! > 0.0 && longitude!! > 0.0) {
                        etLatitude.setText(dformat.format(latitude).toString())
                        etLongitude.setText(dformat.format(longitude).toString())
                        reverseGeoAddress(applicationContext, latitude!!, longitude!!)
                    }
                    dialog.dismiss()
                    mapView!!.onStop()
                } else {
                    Toast.makeText(
                        applicationContext, resources.getString(R.string.need_more_zoom),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            btnClose.setOnClickListener {
                dialog.dismiss()
                //mapView!!.onStop()
            }
            dialog.show()
            val window = dialog.window
            window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        btnSubmitShop.setOnClickListener {
            btnSubmitShop.isEnabled = false
            createShop()
        }
    }

    private fun getShopDetails(shops: Shops) {
        if(shops.route_name.length>0){
            val pos= (spinnerRoutes.adapter as ArrayAdapter<String>).getPosition(shops.route_name)
            if(pos>-1) {
                spinnerRoutes.setSelection(pos)
            }
        }
        etShopName.setText(shops.shop_name)
        if(shops.shop_type.length>0){
            val pos= (spinnerShopType.adapter as ArrayAdapter<String>).getPosition(shops.shop_type)
            if(pos>-1) {
                spinnerShopType.setSelection(pos)
            }
        }
        if(shops.category.length>0){
            val pos= (spinnerCatOutlets.adapter as ArrayAdapter<String>).getPosition(shops.category)
            if(pos>-1) {
                spinnerCatOutlets.setSelection(pos)
            }
        }
        if(shops.market_opportunity.length>0){
            val pos= (spinnerMarketOpportunity.adapter as ArrayAdapter<String>).getPosition(shops.market_opportunity)
            if(pos>-1) {
                spinnerMarketOpportunity.setSelection(pos)
            }
        }
        if(shops.is_buyer >-1){
            //val pos= (spinBuyer.adapter as ArrayAdapter<String>).getPosition(p.getRetailData()[0].is_buyer)
            if (shops.is_buyer == 0 ){
                spinnerBuyer.setSelection(2)
            }else{
                spinnerBuyer.setSelection(1)
            }
        }

        etAddress.setText(shops.address)
        etOwnerName.setText(shops.shop_owner)
        etContactNumber.setText(shops.contact_number)
        etLatitude.setText(shops.latitude.toString())
        etLongitude.setText(shops.longitude.toString())

        if (shops.isVerified == 1){
            isVerified.isChecked = true
        }else{
            isVerified.isChecked = false
        }
    }

    private fun getImageFromDB() {
        var imageList: java.util.ArrayList<Images?>? = java.util.ArrayList()
        imageList = appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as java.util.ArrayList<Images?>?
        if (imageList!!.size > 0){
            for(p in 0 until imageList.size){
                val dbPhotoPath = imageList[p]!!.filePath
                val fileExist : Boolean = File(dbPhotoPath).canRead()
                if(fileExist) {
                    isImageAdded = true
                    imageCounter.visibility = View.VISIBLE
                    imageCounter.text = (imageList.size).toString() + " Photos Added"
                    try {
                        var bitmap = imagepicker.getRotateImage(dbPhotoPath)
                        imagepicker.setLocalImage(bitmap, dbPhotoPath, imageList[p]!!.position, "", "Shop")
                    } catch (e:Exception ) {
                        e.printStackTrace()
                    }
                    Log.d("Imagepos", "ImageList Pos: " + imageList[p]!!.position + "p: " +(p + 1))
                    if (imageList[p]!!.position != p + 1) {
                        Executors.newSingleThreadExecutor().execute {
                                appDatabase!!.imagesDao()!!.updatePosition(dbPhotoPath, p + 1, "Shop")
                            }
                    }
                }else{
                    Executors.newSingleThreadExecutor()
                        .execute { appDatabase!!.imagesDao()!!.deleteImage(p+1, "Shop") }
                }


            }
        }
    }

    var startCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
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
                Log.e("imageUtils", "OnActivity result code 1: " + RESULT_OK)
                var imagePosition = 0
                var imageList: java.util.ArrayList<Images?>? = java.util.ArrayList()
                imageList = appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as java.util.ArrayList<Images?>?
                imageCounter.visibility = View.VISIBLE
                imageCounter.text = (imageList!!.size + 1).toString() + " Photos Added"
                /*if (imageList!!.size > 0){
                    imageCounter.visibility = View.VISIBLE
                    imageCounter.text = (imageList.size+1).toString() + " Photos Added"
                }else{
                    imageCounter.visibility = View.GONE
                }*/
                Log.d("Imagepos", "List: $imageList")
                imagePosition = if (imageList!!.size > 0) {
                    imageList[imageList.size - 1]!!.position + 1
                } else {
                    imagePosition + 1
                }
                imagepicker.AddNewImage(result.data, CAMERA, imagePosition, "Shop")
                try {
                    val placeImage = Images(
                        null, imagePosition,
                        prefs!!.getString(ApiCall.IMAGE_PATH, "")!!,"Shop"
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

    fun getRoutes() {
        ApiServices.apiGET(
            Api.routes_withfilter + "?with_geometry=0&user_id=" + userId,
            queue!!,
            "",
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    if (response != null) {
                        try {
                            val data = JSONObject(response)
                            var routeNameList: java.util.ArrayList<Pair<String, String>>? =
                                ArrayList()
                            if (data.has("routes") && !data.isNull("routes")) {
                                val routesList = java.util.ArrayList<String>()
                                val routesArray = data.getJSONArray("routes")
                                if (routesArray.length() > 0) {
                                    routeNameList!!.add(
                                        Pair(
                                            "",
                                            resources.getString(R.string.select_route)
                                        )
                                    )
                                    routesList.add(resources.getString(R.string.select_route))
                                    for (i in 0 until routesArray.length()) {
                                        val routeObj = routesArray.getJSONObject(i)

                                        routeNameList.add(
                                            Pair(
                                                routeObj.getString("id"),
                                                routeObj.getString("route_name")
                                            )
                                        )
                                        routesList.add(routeObj.getString("route_name"))
                                    }
                                    if (spinnerRoutes != null) {
                                        if (spinnerRoutes.adapter == null) {
                                            val adapter = object : ArrayAdapter<String>(
                                                applicationContext,
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
                                                        view.setTextColor(resources.getColor(R.color.text_title_2))
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

                                        spinnerRoutes.onItemSelectedListener =
                                            object : AdapterView.OnItemSelectedListener {
                                                override fun onItemSelected(
                                                    p0: AdapterView<*>?,
                                                    p1: View?,
                                                    p2: Int,
                                                    p3: Long
                                                ) {
                                                    if (p2 > 0) {
                                                        val view1: TextView =
                                                            p0!!.getChildAt(0) as TextView
                                                        view1.setTextColor(resources.getColor(R.color.black))
                                                        if (routeNameList[p2].first.length > 0) {
                                                            selectedRoute = routeNameList[p2].first
                                                        } else {
                                                            selectedRoute = ""
                                                        }
                                                    } else {
                                                        val view1: TextView =
                                                            p0!!.getChildAt(0) as TextView
                                                        view1.setTextColor(resources.getColor(R.color.text_title_2))
                                                        selectedRoute = ""
                                                    }
                                                }

                                                override fun onNothingSelected(p0: AdapterView<*>?) {

                                                }

                                            }

                                    }
                                }

                            }
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            e.printStackTrace()
                        }
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    TODO("Not yet implemented")
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    TODO("Not yet implemented")
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    e.printStackTrace()
                    Sentry.captureException(e)
                }

            })
    }

    fun setRoutes(routesList: ArrayList<String>, routeNameList: ArrayList<Pair<String, String>>){
        if (spinnerRoutes != null) {
            if (spinnerRoutes.adapter == null) {
                val adapter = object : ArrayAdapter<String>(
                    applicationContext,
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
                            view.setTextColor(resources.getColor(R.color.text_title_2))
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

            spinnerRoutes.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?,
                        p1: View?,
                        p2: Int,
                        p3: Long
                    ) {
                        if (p2 > 0) {
                            val view1: TextView =
                                p0!!.getChildAt(0) as TextView
                            view1.setTextColor(resources.getColor(R.color.black))
                            if (routeNameList[p2].first.length > 0) {
                                selectedRoute = routeNameList[p2].first
                            } else {
                                selectedRoute = ""
                            }
                        } else {
                            val view1: TextView =
                                p0!!.getChildAt(0) as TextView
                            view1.setTextColor(resources.getColor(R.color.text_title_2))
                            selectedRoute = ""
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }

        }
    }

    fun getShopType() {
        ApiServices.apiGET(Api.get_shop_type, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                if (response != null) {
                    try {
                        val typeList: ArrayList<String> = ArrayList()
                        val obj = JSONObject(response)
                        if (obj.has("outlet_types") && !obj.isNull("outlet_types")) {
                            val typesArray = obj.getJSONArray("outlet_types")
                            if (typesArray.length() > 0) {
                                typeList.add(resources.getString(R.string.select_shop_type))
                                for (i in 0 until typesArray.length()) {
                                    typeList.add(typesArray.getString(i))
                                }

                                val shopTypeAdapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item, typeList
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
                                            view.setTextColor(resources.getColor(R.color.text_title_2))
                                            view.visibility = View.GONE
                                        } else {
                                            //here it is possible to define color for other items by
                                            //view.setTextColor(Color.RED)
                                            view.setTextColor(resources.getColor(R.color.black))
                                        }
                                        return view
                                    }
                                }
                                spinnerShopType.adapter = shopTypeAdapter

                                spinnerShopType.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            p0: AdapterView<*>?,
                                            p1: View?,
                                            p2: Int,
                                            p3: Long
                                        ) {
                                            if (p2 > 0) {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.black))
                                                selectedShopType = typeList[p2]
                                            } else {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.text_title_2))
                                                selectedShopType = ""
                                            }
                                        }

                                        override fun onNothingSelected(p0: AdapterView<*>?) {

                                        }

                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                e.printStackTrace()
                Sentry.captureException(e)
            }

        })
    }

    fun getShopCategory() {
        ApiServices.apiGET(Api.get_category_outlet, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                if (response != null) {
                    try {
                        val categoryList: ArrayList<String> = ArrayList()
                        val obj = JSONObject(response)
                        if (obj.has("outlet_categories") && !obj.isNull("outlet_categories")) {
                            val categoryArray = obj.getJSONArray("outlet_categories")
                            if (categoryArray.length() > 0) {
                                categoryList.add(resources.getString(R.string.select_category_of_outlet))
                                for (i in 0 until categoryArray.length()) {
                                    categoryList.add(categoryArray.getString(i))
                                }

                                val shopCategoryAdapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item, categoryList
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
                                            view.setTextColor(resources.getColor(R.color.text_title_2))
                                            view.visibility = View.GONE
                                        } else {
                                            //here it is possible to define color for other items by
                                            //view.setTextColor(Color.RED)
                                            view.setTextColor(resources.getColor(R.color.black))
                                        }
                                        return view
                                    }
                                }
                                spinnerCatOutlets.adapter = shopCategoryAdapter

                                spinnerCatOutlets.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            p0: AdapterView<*>?,
                                            p1: View?,
                                            p2: Int,
                                            p3: Long
                                        ) {
                                            if (p2 > 0) {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.black))
                                                selectedCategory = categoryList[p2]
                                            } else {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.text_title_2))
                                                selectedCategory = ""
                                            }
                                        }

                                        override fun onNothingSelected(p0: AdapterView<*>?) {

                                        }

                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                e.printStackTrace()
                Sentry.captureException(e)
            }

        })
    }

    fun getMarketOpportunity() {
        ApiServices.apiGET(Api.get_market_opportunity, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                if (response != null) {
                    try {
                        val marketOpportunityList: ArrayList<String> = ArrayList()
                        val obj = JSONObject(response)
                        if (obj.has("outlet_market_opportunities") && !obj.isNull("outlet_market_opportunities")) {
                            val marketArray = obj.getJSONArray("outlet_market_opportunities")
                            if (marketArray.length() > 0) {
                                marketOpportunityList.add(resources.getString(R.string.select_market_opportunity))
                                for (i in 0 until marketArray.length()) {
                                    marketOpportunityList.add(marketArray.getString(i))
                                }

                                val marketOpportunityAdapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item, marketOpportunityList
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
                                            view.setTextColor(resources.getColor(R.color.text_title_2))
                                            view.visibility = View.GONE
                                        } else {
                                            //here it is possible to define color for other items by
                                            //view.setTextColor(Color.RED)
                                            view.setTextColor(resources.getColor(R.color.black))
                                        }
                                        return view
                                    }
                                }
                                spinnerMarketOpportunity.adapter = marketOpportunityAdapter

                                spinnerMarketOpportunity.onItemSelectedListener =
                                    object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            p0: AdapterView<*>?,
                                            p1: View?,
                                            p2: Int,
                                            p3: Long
                                        ) {
                                            if (p2 > 0) {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.black))
                                                selectedMarketOpportunity =
                                                    marketOpportunityList[p2]
                                            } else {
                                                val view1: TextView = p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(resources.getColor(R.color.text_title_2))
                                                selectedMarketOpportunity = ""
                                            }
                                        }

                                        override fun onNothingSelected(p0: AdapterView<*>?) {

                                        }

                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                e.printStackTrace()
                Sentry.captureException(e)
            }

        })
    }

    fun getBuyer() {
        val buyerList: ArrayList<String> = ArrayList()
        buyerList.add(resources.getString(R.string.select_buyer))
        buyerList.add(resources.getString(R.string.yes))
        buyerList.add(resources.getString(R.string.no))

        val buyerAdapter = object : ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_spinner_item, buyerList
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: TextView =
                    super.getDropDownView(position, convertView, parent) as TextView
                //set the color of first item in the drop down list to gray
                if (position == 0) {
                    view.setTextColor(resources.getColor(R.color.text_title_2))
                    view.visibility = View.GONE
                } else {
                    //here it is possible to define color for other items by
                    //view.setTextColor(Color.RED)
                    view.setTextColor(resources.getColor(R.color.black))
                }
                return view
            }
        }
        spinnerBuyer.adapter = buyerAdapter

        spinnerBuyer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) {
                    val view1: TextView = p0!!.getChildAt(0) as TextView
                    view1.setTextColor(resources.getColor(R.color.black))

                    if (p2 == 1) {
                        selectedBuyer = 1
                    } else {
                        selectedBuyer = 0
                    }
                } else {
                    val view1: TextView = p0!!.getChildAt(0) as TextView
                    view1.setTextColor(resources.getColor(R.color.text_title_2))
                    selectedBuyer = -1
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    fun createShop() {
        showProgress(progressBarShop)
        var inputOk = true
        if (etShopName.text.trim().length == 0) {
            inputOk = false
            etShopName.setError(getString(R.string.this_field_is_required))
            hideProgress(progressBarShop)
        }
        if (etAddress.text.trim().length == 0) {
            inputOk = false
            etAddress.setError(getString(R.string.this_field_is_required))
            hideProgress(progressBarShop)
        }
        if (etOwnerName.text.trim().length == 0) {
            inputOk = false
            etOwnerName.setError(getString(R.string.this_field_is_required))
            hideProgress(progressBarShop)
        }
        if (selectedRoute!!.length == 0) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Route", Toast.LENGTH_LONG).show()
            hideProgress(progressBarShop)
        }
        if (selectedShopType!!.length == 0) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Shop Type", Toast.LENGTH_LONG).show()
            hideProgress(progressBarShop)
        }
        if (selectedCategory!!.length == 0) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select category outlet", Toast.LENGTH_LONG)
                .show()
            hideProgress(progressBarShop)
        }
        if (latitude == 0.0 || longitude == 0.0) {
            inputOk = false
            Toast.makeText(applicationContext, "Select Shop Location on Map", Toast.LENGTH_LONG)
                .show()
            hideProgress(progressBarShop)
        }

        if (inputOk) {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val byteparams: MutableMap<String, VolleyMultipartRequest.DataPart> = HashMap()
            var imagesList = ArrayList<Images>()
            imagesList = appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as ArrayList<Images>
            if (imagesList.size > 0) {
                for (i in 0 until imagesList.size) {
                    val fileExist = File(imagesList[i].filePath).canRead()
                    if (fileExist) {
                        val imagename = imagesList[i].filePath.substring(
                            imagesList[i].filePath.lastIndexOf("/"))
                        byteparams["images[" + i + "]"] = VolleyMultipartRequest.DataPart(
                            imagename, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                        )
                    }
                }
            }
            val params: MutableMap<String, String> = java.util.HashMap()
            params["outlet_name"] = URLEncoder.encode(etShopName.text.toString(), "utf-8")
            params["outlet_type"] = selectedShopType!!
            params["outlet_category"] = selectedCategory!!
            params["address"] = URLEncoder.encode(etAddress.text.toString(), "utf-8")
            params["owner_name"] = URLEncoder.encode(etOwnerName.text.toString(), "utf-8")
            if (etContactNumber.text.trim().length > 0) params["phone_number"] =
                URLEncoder.encode(etContactNumber.text.toString(), "utf-8")
            if (selectedMarketOpportunity!!.trim().length > 0) params["market_opportunity"] = selectedMarketOpportunity!!
            if (selectedBuyer!! > -1) params["is_buyer"] = selectedBuyer!!.toString()
            params["outlet_created_at"] = today
            params["created_by_user_id"] = userId!!
            params["created_by_employee_id"] = srId!!
            params["route_id"] = selectedRoute!!
            params["latitude"] = latitude.toString()
            params["longitude"] = longitude.toString()

            if (isImageAdded) {
                ApiServices.apiPOSTMultipart(
                    Api.create_shop,
                    queue!!,
                    "",
                    params,
                    byteparams,
                    object : ApiServiceListener {
                        override fun onResponseSuccess(response: String) {

                        }

                        override fun onJSONResponseSuccess(response: JSONObject) {
                            TODO("Not yet implemented")
                        }

                        override fun onNetworkResponseSuccess(response: NetworkResponse) {
                            hideProgress(progressBarShop)
                            val data = JSONObject(String(response.data))
                            val message = data.getString("message")
                            appDatabase!!.imagesDao()!!.deleteAllImages()
                            ViewUtils.viewDialogResponse(
                                this@CreateShopActivity,
                                message,
                                object : DialogListener {
                                    override fun onConfirmed() {
                                        btnSubmitShop.isEnabled = true
                                        finish()
                                        startActivity(getIntent())
                                    }

                                    override fun onCanceled() {
                                        TODO("Not yet implemented")
                                    }

                                })
                        }

                        @RequiresApi(Build.VERSION_CODES.KITKAT)
                        override fun onResponseFailure(error: VolleyError) {
                            btnSubmitShop.isEnabled = true
                            hideProgress(progressBarShop)
                            val s = String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                            )
                            val data = JSONObject(s)
                            val message = data.getString("message")
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        }

                        override fun onException(e: Exception) {
                            btnSubmitShop.isEnabled = true
                            hideProgress(progressBarShop)
                            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                        }

                    })
            }else{
                btnSubmitShop.isEnabled = true
                Toast.makeText(applicationContext, "Need to add Shop image", Toast.LENGTH_SHORT)
                    .show()
            }
        }else{
            btnSubmitShop.isEnabled = true
        }
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
                        etAddress.setText(address+", "+area+", "+city)
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

    @SuppressLint("MissingPermission")
    private fun enableLocation(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(applicationContext)) {

            // Get an instance of the component
            val locationComponent: LocationComponent = mMap!!.getLocationComponent()

            // Activate with options
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(applicationContext, loadedMapStyle)
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
                    LocationEngineCallback<LocationEngineResult> {
                    override fun onSuccess(result: LocationEngineResult?) {
                        val lastLocation = result!!.lastLocation
                        if (lastLocation != null && !lastLocation.equals("null")) {
                            setCameraPosition(
                                LatLng(lastLocation.latitude, lastLocation.longitude),
                                17.0
                            )
                        } else {
                            //locationEngine!!.requestLocationUpdates(locationEngineRequest!!, null)
                            showEnableLocationSetting(this@CreateShopActivity)
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        Toast.makeText(this@CreateShopActivity, exception.message, Toast.LENGTH_SHORT).show()
                    }

                })


            }
        } else {
            permissionsManager = PermissionsManager(this@CreateShopActivity)
            permissionsManager!!.requestLocationPermissions(this@CreateShopActivity)
            //showEnableLocationSetting(this@CreateShopActivity)
        }
    }

    fun showEnableLocationSetting(activity: Activity?) {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val task = LocationServices.getSettingsClient(
            activity!!
        )
            .checkLocationSettings(builder.build())
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
                    e.startResolutionForResult(
                        activity,
                        999
                    )
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
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

    override fun onMapReady(mapboxMap: MapboxMap) {
        mMap = mapboxMap
        mMap!!.setStyle(
            Style.Builder().fromUrl(getString(R.string.map_view_styleUrl)),
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(p0: Style) {
                    enableLocation(p0)
                }

            })


        val uiSettings: UiSettings = mapboxMap!!.uiSettings
        uiSettings.setCompassEnabled(false)

        /*try {
            mapboxMap.style?.let { it1 -> enableLocation(it1) }
        }catch (e: Exception){
            e.printStackTrace()
            Sentry.captureException(e)
        }*/

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
                locationEngine!!.getLastLocation(object :
                    LocationEngineCallback<LocationEngineResult> {
                    override fun onSuccess(result: LocationEngineResult?) {
                        val lastLocation = result!!.lastLocation
                        if (lastLocation != null) {
                            setCameraPosition(
                                LatLng(lastLocation.latitude, lastLocation.longitude),
                                19.0
                            )
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

    private fun showProgress(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress(progressBar: ProgressBar) {
        progressBar.visibility = View.GONE
    }
}