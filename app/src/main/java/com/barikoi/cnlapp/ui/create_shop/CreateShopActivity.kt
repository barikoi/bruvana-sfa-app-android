package com.barikoi.cnlapp.ui.create_shop

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import coil3.load
import coil3.request.crossfade
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.data.remote.models.Outlet
import com.barikoi.cnlapp.databinding.ActivityCreateShopBinding
import com.barikoi.cnlapp.databinding.DialogConfirmBinding
import com.barikoi.cnlapp.imagecapture.RoomDb.ImageDatabase
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import com.barikoi.cnlapp.imagecapture.Utils.ApiCall
import com.barikoi.cnlapp.order_create.Callback.DialogListener
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.ImageUtils
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.VolleyMultipartRequest
import com.barikoi.cnlapp.utils.extension.getParcelableExtraCompat
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.onesignal.common.AndroidSupportV4Compat.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONException
import org.json.JSONObject
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponent
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngine
import org.maplibre.android.location.engine.LocationEngineCallback
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.engine.LocationEngineResult
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.location.permissions.PermissionsListener
import org.maplibre.android.location.permissions.PermissionsManager
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.maps.UiSettings
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
class CreateShopActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {
    private lateinit var binding: ActivityCreateShopBinding

    private var mapView: MapView? = null
    private var mMap: MapLibreMap? = null
    lateinit var fab: FloatingActionButton
    private var permissionsManager: PermissionsManager? = null
    private var locationEngine: LocationEngine? = null
    private var locationEngineRequest: LocationEngineRequest? = null
    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    @Inject
    lateinit var queue: RequestQueue

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    var appDatabase: ImageDatabase? = null
    private var isImageAdded = false
    private var selectedRoute: String? = ""
    private var selectedShopType: String? = ""
    private var selectedCategory: String? = ""
    private var selectedMarketOpportunity: String? = ""
    private var selectedBuyer: Int? = -1
    private var inputVerified: Int? = 0

    private var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()
    private var routesList: ArrayList<String>? = ArrayList()
    var shops: Outlet? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDatabase = ImageDatabase.getInstance(applicationContext)

        val gd = GradientDrawable()
        gd.setColor(ContextCompat.getColor(this, R.color.white))
        gd.cornerRadius = 10f
        gd.setStroke(2, ContextCompat.getColor(this, R.color.required_field))

        binding.spinnerLayoutRoutes.background = gd
        binding.etShopName.background = gd
        binding.spinnerLayoutType.background = gd
        binding.spinnerLayoutCategory.background = gd
        binding.etAddress.background = gd
        binding.etOwnerName.background = gd

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            binding.isVerified.visibility = View.VISIBLE
        } else {
            binding.isVerified.visibility = View.GONE
        }
        if (intent.hasExtra("fromEdit")) {
            binding.btnUpdateShop.visibility = View.VISIBLE
            binding.btnCloseShop.isVisible = true
            binding.btnSubmitShop.visibility = View.GONE
            binding.tvTitle.text = resources.getString(R.string.update_shop_information)
            shops = intent.getParcelableExtraCompat<Outlet>("fromEdit")
            AppLogger.log("SHOP:: $shops")
            getShopDetails(shops!!)
        } else {
            binding.btnUpdateShop.visibility = View.GONE
            binding.btnCloseShop.isVisible = false
            binding.btnSubmitShop.visibility = View.VISIBLE
            binding.tvTitle.text = resources.getString(R.string.new_shop_creation)
        }
        binding.imagePicker.taskId = "taskId"
        binding.imagePicker.CAMERA = 4
        binding.imagePicker.setMainActivity(this@CreateShopActivity)
        binding.imagePicker.setCameraLauncher(startCamera)

        routesList = intent.getStringArrayListExtra("routes")
        routeNameList =
            intent.getParcelableArrayListExtra<Parcelable>("routeList") as ArrayList<Pair<String, String>>

        if (routesList!!.isNotEmpty()) {
            routesList!!.add(0, resources.getString(R.string.select_route))
            routeNameList!!.add(
                0, Pair(
                    "",
                    resources.getString(R.string.select_route)
                )
            )
            setRoutes(routesList!!, routeNameList!!)
        }
        getShopType()
        getMarketOpportunity()
        getBuyer()
        getImageFromDB()

        binding.isVerified.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                inputVerified = 1
            } else {
                inputVerified = 0
            }

        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.locationMap.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.popup_map_view)
            val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
            val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)

            mapView = dialog.findViewById(R.id.map_view)
            mapView!!.onCreate(savedInstanceState)
            mapView!!.getMapAsync(this)
            mapView!!.onStart()

            fab = dialog.findViewById(R.id.fab)

            btnSubmit.setOnClickListener {
                if (mMap!!.cameraPosition.zoom >= 17.0f) {
                    val target = mMap!!.cameraPosition.target
                    latitude = target?.latitude
                    longitude = target?.longitude
                    if (latitude!! > 0.0 && longitude!! > 0.0) {
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

        binding.btnSubmitShop.setOnClickListener {
            binding.btnSubmitShop.isEnabled = false
            createShop()
        }
        binding.btnUpdateShop.setOnClickListener {
            binding.btnUpdateShop.isEnabled = false
            updateShop()
        }

        binding.btnCloseShop.setHapticClickListener {
            if (!isImageAdded) {
                toast("Please add image for close the shop")
                return@setHapticClickListener
            }
            confirmDialog()
        }
    }

    private fun getShopDetails(shops: Outlet) {
        binding.etShopName.setText(shops.outletName)
        binding.etAddress.setText(shops.address)
        binding.etOwnerName.setText(shops.ownerName)
        binding.etContactNumber.setText(shops.phoneNumber)

        latitude = shops.latitude.toDouble()
        longitude = shops.longitude.toDouble()
        inputVerified = shops.isVerified

        if (shops.isVerified == 1) {
            binding.isVerified.isChecked = true
        } else {
            binding.isVerified.isChecked = false
        }

//        if (shops.images.isNotEmpty()) {
////            generateImages(shops.images)
//        }
    }

    private fun generateImages(imageArray: ArrayList<String>) {

        if (!imageArray.isEmpty()) {
            binding.imageViewScroll.visibility = View.VISIBLE
            val layout = findViewById<View>(R.id.imageViewLayout) as LinearLayout
            for (i in 0 until imageArray.size) {
                val image = ImageView(this)
                image.layoutParams = ViewGroup.LayoutParams(200, 200)
                image.maxHeight = 200
                image.maxWidth = 200
                val p = layout.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(8, 8, 4, 8)
                image.layoutParams = p

                val radiusInPx = with(image.context) {
                    resources.displayMetrics.density * 10 // 10dp to px
                }

                image.load(imageArray[i]) {
                    crossfade(true)
                    size(200)
                    transformations(
                        RoundedCornersTransformation(radiusInPx)
                    )
                }

                // Adds the view to the layout
                layout.addView(image)
                image.setOnClickListener {
                    val nagDialog = Dialog(
                        this@CreateShopActivity,
                        android.R.style.Theme_NoTitleBar_Fullscreen
                    )
                    nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    nagDialog.setCancelable(false)
                    nagDialog.setContentView(R.layout.preview_image)
                    val btnClose =
                        nagDialog.findViewById<Button>(R.id.btnIvClose)
                    val ivPreview =
                        nagDialog.findViewById<ImageView>(R.id.iv_preview_image)

                    ivPreview.load(
                        imageArray[i],
                        builder = {
                            crossfade(true)
                            transformations(
                                RoundedCornersTransformation(
                                    10f,
                                    10f,
                                    10f,
                                    10f
                                )
                            )
                        }
                    )
                    btnClose.setOnClickListener { nagDialog.dismiss() }
                    val pAttacher = PhotoViewAttacher(ivPreview)
                    pAttacher.update()
                    nagDialog.show()
                }
            }
        }
    }

    private fun getImageFromDB() {
        appDatabase!!.imagesDao()!!.deleteAllImages()
        val imageList: ArrayList<Images?>? =
            appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as ArrayList<Images?>?
        if (imageList!!.isNotEmpty()) {
            for (p in 0 until imageList.size) {
                val dbPhotoPath = imageList[p]!!.filePath
                val fileExist: Boolean = File(dbPhotoPath).canRead()
                if (fileExist) {
                    isImageAdded = true
                    binding.imageCounter.visibility = View.VISIBLE
                    binding.imageCounter.text = (imageList.size).toString() + " Photos Added"
                    try {
                        val bitmap = binding.imagePicker.getRotateImage(dbPhotoPath)
                        binding.imagePicker.setLocalImage(
                            bitmap,
                            dbPhotoPath,
                            imageList[p]!!.position,
                            "",
                            "Shop"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Log.d("Imagepos", "ImageList Pos: " + imageList[p]!!.position + "p: " + (p + 1))
                    if (imageList[p]!!.position != p + 1) {
                        Executors.newSingleThreadExecutor().execute {
                            appDatabase!!.imagesDao()!!.updatePosition(dbPhotoPath, p + 1, "Shop")
                        }
                    }
                } else {
                    Executors.newSingleThreadExecutor()
                        .execute { appDatabase!!.imagesDao()!!.deleteImage(p + 1, "Shop") }
                }


            }
        }
    }

    private var startCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val filePath = sharePrefUtils.getString(ApiCall.IMAGE_PATH)
        if (result.resultCode == RESULT_CANCELED) {
            if (filePath != null) {
                binding.imagePicker.deleteFileLocal(filePath)
                sharePrefUtils.saveString(ApiCall.IMAGE_PATH, "")
            }
        }
        if (result.resultCode == RESULT_OK) {
            Log.e("imageUtils", "OnActivity result code 1: $RESULT_OK")
            var imagePosition = 0
            val imageList = appDatabase!!.imagesDao()!!
                .getAllImageDB("Shop") as ArrayList<Images?>?
            binding.imageCounter.visibility = View.VISIBLE
            binding.imageCounter.text = (imageList!!.size + 1).toString() + " Photos Added"

            Log.d("Imagepos", "List: $imageList")
            imagePosition = if (imageList.isNotEmpty()) {
                imageList[imageList.size - 1]!!.position + 1
            } else {
                imagePosition + 1
            }
            binding.imagePicker.addNewImage(
                result.data,
                4,
                imagePosition,
                "Shop",
                sharePrefUtils.getString(ApiCall.IMAGE_PATH)!!
            )
            try {
                val placeImage = Images(
                    null, imagePosition,
                    sharePrefUtils.getString(ApiCall.IMAGE_PATH)!!, "Shop"
                )
                isImageAdded = true
                if (imagePosition > 0) {
                    Executors.newSingleThreadExecutor().execute {
                        appDatabase!!.imagesDao()!!.insertAll(placeImage)
                    }
                    sharePrefUtils.saveString(ApiCall.IMAGE_PATH, "")
                }
            } catch (e: java.lang.Exception) {
                Log.e("imageUtils", "OnActivity result 2: $e")
                Sentry.captureException(e)
            }
        }
    }

    fun getRoutes() {
        ApiServices.apiGET(
            Api.routes_withfilter + "?with_geometry=0&user_id=" + sharePrefUtils.getString(Api.USER_ID),
            queue,
            "",
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val data = JSONObject(response)
                        val routeNameList: ArrayList<Pair<String, String>> =
                            ArrayList()
                        if (data.has("routes") && !data.isNull("routes")) {
                            val routesList = ArrayList<String>()
                            val routesArray = data.getJSONArray("routes")
                            if (routesArray.length() > 0) {
                                routeNameList.add(
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
                                if (binding.spinnerRoutes.adapter == null) {
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
                                                view.setTextColor(
                                                    ContextCompat.getColor(
                                                        this@CreateShopActivity,
                                                        R.color.text_title_2
                                                    )
                                                )
                                                view.visibility = View.GONE
                                            } else {
                                                view.setTextColor(
                                                    ContextCompat.getColor(
                                                        this@CreateShopActivity,
                                                        R.color.black
                                                    )
                                                )
                                            }
                                            return view
                                        }
                                    }
                                    binding.spinnerRoutes.adapter = adapter
                                }

                                binding.spinnerRoutes.onItemSelectedListener =
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
                                                view1.setTextColor(
                                                    ContextCompat.getColor(
                                                        this@CreateShopActivity,
                                                        R.color.black
                                                    )
                                                )
                                                if (routeNameList[p2].first.isNotEmpty()) {
                                                    selectedRoute = routeNameList[p2].first
                                                } else {
                                                    selectedRoute = ""
                                                }
                                            } else {
                                                val view1: TextView =
                                                    p0!!.getChildAt(0) as TextView
                                                view1.setTextColor(
                                                    ContextCompat.getColor(
                                                        this@CreateShopActivity,
                                                        R.color.text_title_2
                                                    )
                                                )
                                                selectedRoute = ""
                                            }
                                        }

                                        override fun onNothingSelected(p0: AdapterView<*>?) {}

                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    e.printStackTrace()
                    Sentry.captureException(e)
                }
            })
    }

    private fun setRoutes(
        routesList: ArrayList<String>,
        routeNameList: ArrayList<Pair<String, String>>
    ) {
        var storedRoute = sharePrefUtils.getString(Api.SELECTED_ROUTE_NAME_LIST)!!
        if (binding.spinnerRoutes.adapter == null) {
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
                    if (position == 0) {
                        view.setTextColor(
                            ContextCompat.getColor(
                                this@CreateShopActivity,
                                R.color.text_title_2
                            )
                        )
                        view.visibility = View.GONE
                    } else {
                        view.setTextColor(
                            ContextCompat.getColor(
                                this@CreateShopActivity,
                                R.color.black
                            )
                        )
                    }
                    return view
                }
            }
            binding.spinnerRoutes.adapter = adapter
        }

        binding.spinnerRoutes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    if (p0 != null) {
                        if (storedRoute.isNotEmpty()) {
                            if (binding.spinnerRoutes.adapter.count > 0) {
                                val pos =
                                    (binding.spinnerRoutes.adapter as ArrayAdapter<String>)
                                        .getPosition(storedRoute)
                                if (pos > -1) {
                                    storedRoute = ""
                                    binding.spinnerRoutes.setSelection(pos)
                                }
                            }
                        }
                        if (p0.isNotEmpty()) {
                            if (p2 > 0) {
                                val view1: TextView = p0.getChildAt(0) as TextView
                                view1.setTextColor(
                                    ContextCompat.getColor(
                                        this@CreateShopActivity,
                                        R.color.black
                                    )
                                )
                                if (routeNameList[p2].first.isNotEmpty()) {
                                    selectedRoute = routeNameList[p2].first
                                } else {
                                    selectedRoute = ""
                                }
                            } else {
                                val view1: TextView = p0.getChildAt(0) as TextView
                                view1.setTextColor(
                                    ContextCompat.getColor(
                                        this@CreateShopActivity,
                                        R.color.text_title_2
                                    )
                                )
                                selectedRoute = ""
                            }
                        }
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }

        if (shops != null) {
            if (shops!!.routeName!!.isNotEmpty()) {
                val pos =
                    (binding.spinnerRoutes.adapter as ArrayAdapter<String>).getPosition(shops!!.routeName)
                if (pos > -1) {
                    binding.spinnerRoutes.setSelection(pos)
                }
            }
        }
    }

    private fun getShopType() {
        ApiServices.apiGET(Api.get_shop_type, queue, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
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
                                        view.setTextColor(
                                            ContextCompat.getColor(
                                                this@CreateShopActivity,
                                                R.color.text_title_2
                                            )
                                        )
                                        view.visibility = View.GONE
                                    } else {
                                        view.setTextColor(
                                            ContextCompat.getColor(
                                                this@CreateShopActivity,
                                                R.color.black
                                            )
                                        )
                                    }
                                    return view
                                }
                            }
                            binding.spinnerShopType.adapter = shopTypeAdapter

                            binding.spinnerShopType.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        p0: AdapterView<*>?,
                                        p1: View?,
                                        p2: Int,
                                        p3: Long
                                    ) {
                                        if (p2 > 0) {
                                            val view1: TextView = p0!!.getChildAt(0) as TextView
                                            view1.setTextColor(
                                                ContextCompat.getColor(
                                                    this@CreateShopActivity,
                                                    R.color.black
                                                )
                                            )
                                            selectedShopType = typeList[p2]
                                        } else {
                                            val view1: TextView = p0!!.getChildAt(0) as TextView
                                            view1.setTextColor(
                                                ContextCompat.getColor(
                                                    this@CreateShopActivity,
                                                    R.color.text_title_2
                                                )
                                            )
                                            selectedShopType = ""
                                        }
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }

                                }

                            if (shops != null) {
                                typeList.mapIndexed { pos, s ->
                                    if (shops!!.outletType.trim() == s.trim()) {
                                        binding.spinnerShopType.setSelection(pos)
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

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                e.printStackTrace()
                Sentry.captureException(e)
            }

        })
    }

    private fun confirmDialog() {
        val dialogBinding = DialogConfirmBinding.inflate(LayoutInflater.from(this))

        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnYes.setOnClickListener {
            updateShop(true)
            dialog.dismiss()
        }
        dialogBinding.btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getMarketOpportunity() {
        ApiServices.apiGET(Api.get_market_opportunity, queue, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
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
                                        view.setTextColor(
                                            ContextCompat.getColor(
                                                this@CreateShopActivity,
                                                R.color.text_title_2
                                            )
                                        )
                                        view.visibility = View.GONE
                                    } else {
                                        //here it is possible to define color for other items by
                                        //view.setTextColor(Color.RED)
                                        view.setTextColor(
                                            ContextCompat.getColor(
                                                this@CreateShopActivity,
                                                R.color.black
                                            )
                                        )
                                    }
                                    return view
                                }
                            }
                            binding.spinnerMarketOpportunity.adapter = marketOpportunityAdapter

                            binding.spinnerMarketOpportunity.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        p0: AdapterView<*>?,
                                        p1: View?,
                                        p2: Int,
                                        p3: Long
                                    ) {
                                        if (p2 > 0) {
                                            val view1: TextView = p0!!.getChildAt(0) as TextView
                                            view1.setTextColor(
                                                ContextCompat.getColor(
                                                    this@CreateShopActivity,
                                                    R.color.black
                                                )
                                            )
                                            selectedMarketOpportunity =
                                                marketOpportunityList[p2]
                                        } else {
                                            val view1: TextView = p0!!.getChildAt(0) as TextView
                                            view1.setTextColor(
                                                ContextCompat.getColor(
                                                    this@CreateShopActivity,
                                                    R.color.text_title_2
                                                )
                                            )
                                            selectedMarketOpportunity = ""
                                        }
                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {

                                    }

                                }

                            if (shops != null) {
                                if (shops!!.marketOpportunity!!.isNotEmpty()) {
                                    val pos =
                                        (binding.spinnerMarketOpportunity.adapter as ArrayAdapter<String>).getPosition(
                                            shops!!.marketOpportunity
                                        )
                                    if (pos > -1) {
                                        binding.spinnerMarketOpportunity.setSelection(pos)
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

            override fun onJSONResponseSuccess(response: JSONObject) {
                throw NotImplementedError("This function is not implemented yet.")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                throw NotImplementedError("This function is not implemented yet.")
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

    private fun getBuyer() {
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
                if (position == 0) {
                    view.setTextColor(
                        ContextCompat.getColor(
                            this@CreateShopActivity,
                            R.color.text_title_2
                        )
                    )
                    view.visibility = View.GONE
                } else {
                    view.setTextColor(
                        ContextCompat.getColor(
                            this@CreateShopActivity,
                            R.color.black
                        )
                    )
                }
                return view
            }
        }
        binding.spinnerBuyer.adapter = buyerAdapter

        binding.spinnerBuyer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) {
                    val view1: TextView = p0!!.getChildAt(0) as TextView
                    view1.setTextColor(
                        ContextCompat.getColor(
                            this@CreateShopActivity,
                            R.color.black
                        )
                    )

                    selectedBuyer = if (p2 == 1) {
                        1
                    } else {
                        0
                    }
                } else {
                    val view1: TextView = p0!!.getChildAt(0) as TextView
                    view1.setTextColor(
                        ContextCompat.getColor(
                            this@CreateShopActivity,
                            R.color.text_title_2
                        )
                    )
                    selectedBuyer = -1
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }

        if (shops != null) {
            shops!!.isBuyer?.let {
                if (it > -1) {
                    if (shops!!.isBuyer == 0) {
                        binding.spinnerBuyer.setSelection(2)
                    } else {
                        binding.spinnerBuyer.setSelection(1)
                    }
                }
            }
        }
    }

    private fun createShop() {
        showProgress(binding.progressBarShop)
        var inputOk = true
        if (binding.etShopName.text.trim().isEmpty()) {
            inputOk = false
            binding.etShopName.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (binding.etAddress.text.trim().isEmpty()) {
            inputOk = false
            binding.etAddress.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (binding.etOwnerName.text.trim().isEmpty()) {
            inputOk = false
            binding.etOwnerName.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (selectedRoute!!.isEmpty()) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Route", Toast.LENGTH_LONG).show()
            hideProgress(binding.progressBarShop)
        }
        if (selectedShopType!!.isEmpty()) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Shop Type", Toast.LENGTH_LONG).show()
            hideProgress(binding.progressBarShop)
        }
//        if (selectedCategory!!.isEmpty()) {
//            inputOk = false
//            Toast.makeText(applicationContext, "Need to select category outlet", Toast.LENGTH_LONG)
//                .show()
//            hideProgress(binding.progressBarShop)
//        }
        if (latitude == 0.0 || longitude == 0.0) {
            inputOk = false
            Toast.makeText(applicationContext, "Select Shop Location on Map", Toast.LENGTH_LONG)
                .show()
            hideProgress(binding.progressBarShop)
        }

        if (inputOk) {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val byteparams: MutableMap<String, VolleyMultipartRequest.DataPart> = HashMap()
            val imagesList: ArrayList<Images> =
                appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as ArrayList<Images>
            if (imagesList.isNotEmpty()) {
                for (i in 0 until imagesList.size) {
                    val fileExist = File(imagesList[i].filePath).canRead()
                    if (fileExist) {
                        val imagename = imagesList[i].filePath.substring(
                            imagesList[i].filePath.lastIndexOf("/")
                        )
                        byteparams["images[$i]"] = VolleyMultipartRequest.DataPart(
                            imagename, ImageUtils.decodeFile(imagesList[i].filePath), "image/jpeg"
                        )
                    }
                }
            }
            val params: MutableMap<String, String> = HashMap()
            params["outlet_name"] = URLEncoder.encode(binding.etShopName.text.toString(), "utf-8")
            params["outlet_type"] = selectedShopType!!
            params["address"] = URLEncoder.encode(binding.etAddress.text.toString(), "utf-8")
            params["owner_name"] = URLEncoder.encode(binding.etOwnerName.text.toString(), "utf-8")
            if (binding.etContactNumber.text.trim().isNotEmpty()) params["phone_number"] =
                URLEncoder.encode(binding.etContactNumber.text.toString(), "utf-8")

            params["outlet_created_at"] = today
            params["created_by_user_id"] = sharePrefUtils.getString(Api.USER_ID)!!
            params["created_by_employee_id"] = sharePrefUtils.getString(Api.EMPLOYEE_ID)!!
            params["route_id"] = selectedRoute!!
            params["latitude"] = latitude.toString()
            params["longitude"] = longitude.toString()
            params["is_verified"] = inputVerified.toString()

            if (isImageAdded) {
                ApiServices.apiPOSTMultipart(
                    Api.create_shop,
                    queue,
                    "",
                    params,
                    byteparams,
                    object : ApiServiceListener {
                        override fun onResponseSuccess(response: String) {

                        }

                        override fun onJSONResponseSuccess(response: JSONObject) {
                        }

                        override fun onNetworkResponseSuccess(response: NetworkResponse) {
                            try {
                                hideProgress(binding.progressBarShop)
                                val data = JSONObject(String(response.data))
                                val message = data.getString("message")
                                appDatabase!!.imagesDao()!!.deleteAllImages()
                                ViewUtils.viewDialogResponse(
                                    this@CreateShopActivity,
                                    message,
                                    object : DialogListener {
                                        override fun onConfirmed() {
                                            binding.btnSubmitShop.isEnabled = true
                                            finish()
                                            startActivity(getIntent())
                                        }

                                        override fun onCanceled() {
                                        }

                                    })
                            } catch (e: Exception) {
                                Sentry.captureException(e)
                                Toast.makeText(
                                    applicationContext,
                                    e.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onResponseFailure(error: VolleyError) {
                            try {
                                binding.btnSubmitShop.isEnabled = true
                                hideProgress(binding.progressBarShop)
                                val s = String(
                                    error.networkResponse.data,
                                    StandardCharsets.UTF_8
                                )
                                val data = JSONObject(s)
                                val message = data.getString("message")
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: Exception) {
                                Sentry.captureException(e)
                                Toast.makeText(
                                    applicationContext,
                                    e.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onException(e: Exception) {
                            try {
                                binding.btnSubmitShop.isEnabled = true
                                hideProgress(binding.progressBarShop)
                                Toast.makeText(
                                    applicationContext,
                                    e.message.toString(),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } catch (e: Exception) {
                                Sentry.captureException(e)
                                Toast.makeText(
                                    applicationContext,
                                    e.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    })
            } else {
                hideProgress(binding.progressBarShop)
                binding.btnSubmitShop.isEnabled = true
                Toast.makeText(applicationContext, "Need to add Shop image", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            hideProgress(binding.progressBarShop)
            binding.btnSubmitShop.isEnabled = true
        }
    }

    fun updateShop(isClosedShop: Boolean = false) {
        showProgress(binding.progressBarShop)
        var inputOk = true
        if (binding.etShopName.text.trim().isEmpty()) {
            inputOk = false
            binding.etShopName.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (binding.etAddress.text.trim().isEmpty()) {
            inputOk = false
            binding.etAddress.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (binding.etOwnerName.text.trim().isEmpty()) {
            inputOk = false
            binding.etOwnerName.error = getString(R.string.this_field_is_required)
            hideProgress(binding.progressBarShop)
        }
        if (selectedRoute!!.isEmpty()) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Route", Toast.LENGTH_LONG).show()
            hideProgress(binding.progressBarShop)
        }
        if (selectedShopType!!.isEmpty()) {
            inputOk = false
            Toast.makeText(applicationContext, "Need to select Shop Type", Toast.LENGTH_LONG).show()
            hideProgress(binding.progressBarShop)
        }
        if (latitude == 0.0 || longitude == 0.0) {
            inputOk = false
            Toast.makeText(applicationContext, "Select Shop Location on Map", Toast.LENGTH_LONG)
                .show()
            hideProgress(binding.progressBarShop)
        }

        if (inputOk) {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val today = df.format(Calendar.getInstance().time)
            val byteParams: MutableMap<String, VolleyMultipartRequest.DataPart> = HashMap()
            val imagesList: ArrayList<Images> =
                appDatabase!!.imagesDao()!!.getAllImageDB("Shop") as ArrayList<Images>
            if (imagesList.isNotEmpty()) {
                for (i in 0 until imagesList.size) {
                    val fileExist = File(imagesList[i].filePath).canRead()
                    if (fileExist) {
                        val imageName = imagesList[i].filePath.substring(
                            imagesList[i].filePath.lastIndexOf("/")
                        )
                        byteParams["images[$i]"] =
                            VolleyMultipartRequest.DataPart(
                                imageName,
                                ImageUtils.decodeFile(imagesList[i].filePath),
                                "image/jpeg"
                            )
                    }
                }
            }
            val params: MutableMap<String, String> = HashMap()
            params["outlet_name"] = URLEncoder.encode(binding.etShopName.text.toString(), "utf-8")
            params["outlet_id"] = shops!!.id.toString()
            params["outlet_type"] = selectedShopType!!
            params["address"] = URLEncoder.encode(binding.etAddress.text.toString(), "utf-8")
            params["owner_name"] = URLEncoder.encode(binding.etOwnerName.text.toString(), "utf-8")
            if (binding.etContactNumber.text.trim().isNotEmpty()) params["phone_number"] =
                URLEncoder.encode(binding.etContactNumber.text.toString(), "utf-8")
            if (selectedMarketOpportunity!!.trim().isNotEmpty()) params["market_opportunity"] =
                selectedMarketOpportunity!!
            if (selectedBuyer!! > -1) params["is_buyer"] = selectedBuyer!!.toString()
            params["outlet_updated_at"] = today
            params["updated_by_user_id"] = sharePrefUtils.getString(Api.USER_ID)!!
            params["updated_by_employee_id"] = sharePrefUtils.getString(Api.EMPLOYEE_ID)!!
            params["route_id"] = selectedRoute!!
            params["latitude"] = latitude.toString()
            params["longitude"] = longitude.toString()
            params["is_verified"] = inputVerified.toString()
            params["is_closed"] = if (isClosedShop) "1" else "0"

            ApiServices.apiPOSTMultipart(
                Api.update_shop,
                queue,
                "",
                params,
                byteParams,
                object : ApiServiceListener {
                    override fun onResponseSuccess(response: String) {

                    }

                    override fun onJSONResponseSuccess(response: JSONObject) {

                    }

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {
                        try {
                            hideProgress(binding.progressBarShop)
                            val data = JSONObject(String(response.data))
                            val message = data.getString("message")
                            appDatabase!!.imagesDao()!!.deleteAllImages()
                            ViewUtils.viewDialogResponse(
                                this@CreateShopActivity,
                                message,
                                object : DialogListener {
                                    override fun onConfirmed() {
                                        binding.btnUpdateShop.isEnabled = true
                                        setResult(55)
                                        finish()
                                    }

                                    override fun onCanceled() {
                                    }

                                })
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            Toast.makeText(
                                applicationContext,
                                e.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onResponseFailure(error: VolleyError) {
                        try {
                            binding.btnUpdateShop.isEnabled = true
                            hideProgress(binding.progressBarShop)
                            val s = String(
                                error.networkResponse.data,
                                StandardCharsets.UTF_8
                            )
                            val data = JSONObject(s)
                            val message = data.getString("message")
                            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            Toast.makeText(
                                applicationContext,
                                e.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onException(e: Exception) {
                        try {
                            binding.btnUpdateShop.isEnabled = true
                            hideProgress(binding.progressBarShop)
                            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                            Toast.makeText(
                                applicationContext,
                                e.message.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                })
        } else {
            binding.btnUpdateShop.isEnabled = true
        }
    }

    private fun reverseGeoAddress(context: Context, lat: Double, lng: Double) {
        try {
            val queue = RequestQueueSingleton.getInstance(context.applicationContext).requestQueue
            val request: StringRequest = object : StringRequest(
                Method.GET,
                Api.reverseGeo + "?key=" + BuildConfig.TRACE_API_KEY + "&latitude=" + lat + "&longitude=" + lng,
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
                        binding.etAddress.setText(address + ", " + area + ", " + city)
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
            val locationComponent: LocationComponent = mMap!!.locationComponent

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
                            showEnableLocationSetting(this@CreateShopActivity)
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        Toast.makeText(
                            this@CreateShopActivity,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            }
        } else {
            permissionsManager = PermissionsManager(this@CreateShopActivity)
            permissionsManager!!.requestLocationPermissions(this@CreateShopActivity)
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
                }
            }
        }
    }

    suspend fun checkLocationSettings(activity: Activity): Boolean =
        suspendCancellableCoroutine { cont ->
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(
                    LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY, 10_000L
                    ).build()
                )

            val client = LocationServices.getSettingsClient(activity)
            val task = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                cont.resume(response.locationSettingsStates?.isLocationPresent == true)
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(activity, 2000)
                    } catch (sendEx: SendIntentException) {
                        cont.resume(false)
                    }
                } else {
                    cont.resume(false)
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

    override fun onMapReady(mapboxMap: MapLibreMap) {
        mMap = mapboxMap
        mMap!!.setStyle(
            Style.Builder().fromUrl(getString(R.string.map_view_styleUrl))
        ) { p0 -> enableLocation(p0) }


        val uiSettings: UiSettings = mapboxMap.uiSettings
        uiSettings.isCompassEnabled = false

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