package com.barikoi.cnlapp.Attendance.Fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.LocationFetch
import com.barikoi.cnlapp.databinding.FragmentCreateAttendanceBinding
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
import com.barikoi.cnlapp.utils.extension.formatDateToFullName
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.rotateViewAnimation
import com.barikoi.cnlapp.utils.extension.toast
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.Date
import java.util.concurrent.Executors
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class CreateAttendanceFragment : Fragment() {
    private lateinit var binding: FragmentCreateAttendanceBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var mQueue: RequestQueue

    private lateinit var appDatabase: ImageDatabase
    private var isImageAdded = false
    var selectedRoute: String = ""
    var routeId: Int? = null

    private lateinit var checkAttendanceDialog: Dialog
    var routeNameList: ArrayList<Pair<String, String>>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appDatabase = ImageDatabase.getInstance(requireContext())!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().loadingDialog {
            checkAttendanceDialog = it
        }
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        binding.tvDate.text = Date().formatDateToFullName()

        binding.attendanceImagePicker.taskId = "taskId"
        binding.attendanceImagePicker.CAMERA = 4
        binding.attendanceImagePicker.setMainActivity(requireActivity())
        binding.attendanceImagePicker.setFragment(this)
        binding.attendanceImagePicker.setCameraLauncher(startCamera)


        getImageFromDB()

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true) || sharePrefUtils.getString(
                Api.USER_TYPE
            ).equals("ASM")
        ) {
            binding.spinnerLayoutRoute.visibility = View.GONE
            binding.titleRoute.visibility = View.GONE
        } else {
            binding.spinnerLayoutRoute.visibility = View.VISIBLE
            binding.titleRoute.visibility = View.VISIBLE
            getAllRoutes(
                Api.routes_withfilter + "?with_geometry=0&user_id=" + sharePrefUtils.getString(
                    Api.USER_ID
                )
            )


            binding.spinnerRoutes.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        if (p2 > 0) {
                            val view1: TextView =
                                p0!!.getChildAt(0) as TextView
                            view1.setTextColor(requireContext().resources.getColor(R.color.black))
                            if (routeNameList!![p2].first.isNotEmpty()) {
                                routeId = routeNameList!![p2].first.toInt()
                                selectedRoute = routeNameList!![p2].second
                            } else {
                                routeId = null
                                selectedRoute = ""
                            }
                        } else {
                            if (p0!!.getChildAt(0) == null)
                                return
                            val view1: TextView =
                                p0.getChildAt(0) as TextView
                            view1.setTextColor(requireContext().resources.getColor(R.color.text_title_2))
                            routeId = null
                            selectedRoute = ""
                        }
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}

                }
        }

        getLocation("reversegeo")
        binding.imgRefresh.rotateViewAnimation(0f, 380f)

        checkAttendance()

        binding.imgRefresh.setOnClickListener {
            binding.imgRefresh.rotateViewAnimation(0f, 380f)
            getLocation("reversegeo")
        }

        binding.btnCheckIn.setOnClickListener {
            if (sharePrefUtils.getString(Api.USER_TYPE)
                    .equals("TO", true) || sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")
            ) {
                if (isImageAdded) {
                    binding.progressBar.visibility = View.VISIBLE
                    getLocation("check_in")
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Upload image for attendance",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            } else {
                if (selectedRoute.isNotEmpty() && isImageAdded) {
                    binding.progressBar.visibility = View.VISIBLE
                    getLocation("check_in")
                } else {
                    if (!isImageAdded) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.upload_image_for_attendance),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    if (selectedRoute.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Select route for check in",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }
        binding.btnCheckOut.setOnClickListener {
            if (isImageAdded) {
                showCheckoutConfirmDialog()
            } else {
                Toast.makeText(requireContext(), "Upload image for check out", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnCheckedAlready.setOnClickListener {
            checkAttendance()
        }
    }

    // create a confirm dialog use this function to show a dialog before checking out
    private fun showCheckoutConfirmDialog() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.confirm))
            .setMessage(getString(R.string.are_you_sure_you_want_to_checkout))
            .setPositiveButton(getString(R.string.check_out)) { dialog, _ ->
                dialog.dismiss()
                getLocation("check_out")

                binding.progressBar.visibility = View.VISIBLE
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun checkAttendance() {
        checkAttendanceDialog.show()
        ApiServices.apiGET(
            Api.check_today_attendance,
            mQueue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    checkAttendanceDialog.dismiss()
                    try {
                        val data = JSONObject(response)
                        AppLogger.log("ATTENDANCE CHECK $data")

                        if (data.has("attendances") && !data.isNull("attendances")) {
                            val attendanceObj = data.getJSONObject("attendances")
                            val checkIn = attendanceObj.getString("checkin_time")
                            val checkOut = attendanceObj.getString("checkout_time")

                            if (checkIn.equals("null") && checkOut.equals("null")) {
                                binding.btnCheckIn.isVisible = true
                                binding.btnCheckOut.isVisible = false
                                binding.btnCheckedAlready.isVisible = false
                                binding.llImagePickerView.isVisible = false
                                binding.attendanceImagePicker.isVisible = true


                                appDatabase.imagesDao()!!.deleteAllImages()
                            } else if (!checkIn.equals("null") && checkOut.equals("null")) {
                                binding.btnCheckIn.isVisible = false
                                binding.btnCheckOut.isVisible = true
                                binding.btnCheckedAlready.isVisible = false

                                binding.spinnerRoutes.isEnabled = false
                                binding.attendanceImagePicker.isVisible = true
                                binding.llImagePickerView.isVisible = false
                                binding.editTextReason.isEnabled = false

                            } else {
                                binding.btnCheckIn.isVisible = false
                                binding.btnCheckOut.isVisible = false
                                binding.btnCheckedAlready.isVisible = true

                                binding.spinnerRoutes.isEnabled = false
                                binding.attendanceImagePicker.isVisible = false
                                binding.llImagePickerView.isVisible = true
                                binding.editTextReason.isEnabled = false


                                appDatabase.imagesDao()!!.deleteAllImages()
                            }

                        } else {
                            binding.btnCheckIn.isVisible = true
                            binding.btnCheckOut.isVisible = false
                            binding.btnCheckedAlready.isVisible = false
                            binding.llImagePickerView.isVisible = false
                            binding.attendanceImagePicker.isVisible = true

                            appDatabase.imagesDao()!!.deleteAllImages()
                        }

                    } catch (e: Exception) {
                        checkAttendanceDialog.dismiss()
                        toast("Error: ${e.message}")
                        Sentry.captureException(e)
                        e.printStackTrace()


                        appDatabase.imagesDao()!!.deleteAllImages()
                    }

                    getImageFromDB()
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    checkAttendanceDialog.dismiss()
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {
                    checkAttendanceDialog.dismiss()
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getImageFromDB() {
        val imageList: ArrayList<Images?>? =
            appDatabase.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images?>?

        if (imageList!!.isNotEmpty()) {
            for (p in 0 until imageList.size) {
                val dbPhotoPath = imageList[p]!!.filePath
                val fileExist: Boolean = File(imageList[p]!!.filePath).canRead()

                if (fileExist) {
                    try {
                        val bitmap = binding.attendanceImagePicker.getRotateImage(dbPhotoPath)
                        isImageAdded = true
                        binding.attendanceImagePicker.setLocalImage(
                            bitmap,
                            dbPhotoPath,
                            imageList[p]!!.position,
                            "",
                            "Attendance"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    Log.d(
                        "Image POS",
                        "ImageList Pos: " + imageList[p]!!.position + "p: " + (p + 1)
                    )

                    if (imageList[p]!!.position != p + 1) {
                        Executors.newSingleThreadExecutor().execute {
                            appDatabase.imagesDao()!!
                                .updatePosition(dbPhotoPath, p + 1, "Attendance")
                        }
                    }

                } else {
                    Executors.newSingleThreadExecutor()
                        .execute { appDatabase.imagesDao()!!.deleteImage(p + 1, "Attendance") }
                }
            }
        }else {
            isImageAdded = false
            binding.attendanceImagePicker.removeImages()
        }
    }

    fun checkInAttendance(location: Location) {
        val byteParams: MutableMap<String, VolleyMultipartRequest.DataPart> = HashMap()
        val imagesList: ArrayList<Images> =
            appDatabase.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images>
        if (imagesList.isNotEmpty()) {
            val fileExist = File(imagesList[0].filePath).canRead()
            if (fileExist) {
                val imagename = imagesList[0].filePath.substring(
                    imagesList[0].filePath.lastIndexOf("/")
                )
                byteParams["images[0]"] = VolleyMultipartRequest.DataPart(
                    imagename, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                )
            }
        }
        val params: MutableMap<String, String> = HashMap()
        params["type"] = "checkin"
        params["latitude"] = location.latitude.toString()
        params["longitude"] = location.longitude.toString()
        if (routeId != null) params["route_id"] = routeId.toString()

        if (binding.editTextReason.text.toString().isNotEmpty())
            params["remarks"] = binding.editTextReason.text.toString()

        ApiServices.apiPOSTMultipart(
            Api.check_in,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            params,
            byteParams,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {}

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    binding.progressBar.visibility = View.GONE
                    val data = JSONObject(String(response.data))
                    val message = data.getString("message")

                    ViewUtils.startTracking(requireActivity(), requireContext())

                    checkAttendance()
                    ViewUtils.viewDialogResponse(
                        requireContext(),
                        message,
                        object : DialogListener {
                            override fun onConfirmed() {
                                sharePrefUtils.saveString(Api.SELECTED_ROUTE_ID, routeId.toString())
                                sharePrefUtils.saveString(Api.SELECTED_ROUTE_NAME, selectedRoute)
                            }

                            override fun onCanceled() {}

                        })
                }

                override fun onResponseFailure(error: VolleyError) {
                    binding.progressBar.visibility = View.GONE
                    val s = String(
                        error.networkResponse.data,
                        StandardCharsets.UTF_8
                    )
                    val data = JSONObject(s)
                    val message = data.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }

                override fun onException(e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun checkOutAttendance(location: Location) {
        val byteParams: MutableMap<String, VolleyMultipartRequest.DataPart> = HashMap()
        val imagesList: List<Images> =
            appDatabase.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images>
        if (imagesList.isNotEmpty()) {
            val fileExist = File(imagesList[0].filePath).canRead()
            if (fileExist) {
                val imageName = imagesList[0].filePath.substring(
                    imagesList[0].filePath.lastIndexOf("/")
                )
                byteParams["images[0]"] = VolleyMultipartRequest.DataPart(
                    imageName, ImageUtils.decodeFile(imagesList[0].filePath), "image/jpeg"
                )
            }
        }
        val params: MutableMap<String, String> = HashMap()
        params["latitude"] = location.latitude.toString()
        params["longitude"] = location.longitude.toString()

        ApiServices.apiPOSTMultipart(
            Api.check_out,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            params,
            byteParams,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {}

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    binding.progressBar.visibility = View.GONE
                    val data = JSONObject(String(response.data))
                    val message = data.getString("message")

                    if (BarikoiTrace.isLocationTracking()) {
                        BarikoiTrace.stopTracking()
                    }

                    appDatabase.imagesDao()!!.deleteAllImages()
                    ViewUtils.viewDialogResponse(
                        requireContext(),
                        message,
                        object : DialogListener {
                            override fun onConfirmed() {
                                sharePrefUtils.saveString(Api.SELECTED_ROUTE_ID, routeId.toString())
                                sharePrefUtils.saveString(Api.SELECTED_ROUTE_NAME, selectedRoute)
                                checkAttendance()
                            }

                            override fun onCanceled() {}

                        })
                }

                override fun onResponseFailure(error: VolleyError) {
                    binding.progressBar.visibility = View.GONE
                    val s = String(
                        error.networkResponse.data,
                        StandardCharsets.UTF_8
                    )
                    val data = JSONObject(s)
                    val message = data.getString("message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }

                override fun onException(e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun getLocation(choice: String) {
        ViewUtils.getLocation(requireContext(), requireActivity(), object : LocationFetch {
            override fun onFetchSuccess(location: Location) {
                when (choice) {
                    "check_in" -> {
                        checkInAttendance(location)
                    }

                    "check_out" -> {
                        checkOutAttendance(location)
                    }

                    "reversegeo" -> {
                        reverseGeoAddress(requireContext(), location.latitude, location.longitude)
                    }
                }
            }

            override fun onFailure() {}

        })
    }

    private fun getAllRoutes(url: String) {
        routeNameList!!.clear()
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    //loading!!.visibility = View.GONE
                    val data = JSONObject(response)
                    if (data.has("routes") && !data.isNull("routes")) {
                        val routesList = ArrayList<String>()
                        val routesArray = data.getJSONArray("routes")
                        if (routesArray.length() > 0) {
                            routeNameList!!.add(
                                Pair(
                                    "",
                                    requireContext().resources.getString(R.string.select_route)
                                )
                            )
                            routesList.add(requireContext().resources.getString(R.string.select_route))
                            for (i in 0 until routesArray.length()) {
                                val routeObj = routesArray.getJSONObject(i)

                                routeNameList!!.add(
                                    Pair(
                                        routeObj.getString("id"),
                                        routeObj.getString("route_name")
                                    )
                                )
                                routesList.add(routeObj.getString("route_name"))
                            }
                            if (binding.spinnerRoutes.adapter == null) {
                                val adapter = object : ArrayAdapter<String>(
                                    requireContext(),
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
                                                requireContext().resources.getColor(
                                                    R.color.text_title_2
                                                )
                                            )
                                            view.visibility = View.GONE
                                        } else {
                                            //here it is possible to define color for other items by
                                            //view.setTextColor(Color.RED)
                                            view.setTextColor(resources.getColor(R.color.black))
                                        }
                                        return view
                                    }
                                }
                                binding.spinnerRoutes.adapter = adapter
                            }

                            if (sharePrefUtils.getString(Api.SELECTED_ROUTE_ID)!!
                                    .isNotEmpty()
                            ) {
                                val selectedRouteId =
                                    sharePrefUtils.getString(Api.SELECTED_ROUTE_ID)
                                for (i in 0 until routeNameList!!.size) {
                                    if (routeNameList!![i].first == selectedRouteId) {
                                        binding.spinnerRoutes.setSelection(i)
                                        break
                                    }
                                }
                            } else {
                                binding.spinnerRoutes.setSelection(0)
                            }


                        }

                    }
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                //loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    Toast.makeText(
                        requireContext(),
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    Toast.makeText(
                        requireContext(),
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error?.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        Toast.makeText(
                            requireContext(),
                            data.getString("message"),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        Sentry.captureException(e)
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        request.retryPolicy = DefaultRetryPolicy(
            60 * 1000, 0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        mQueue.add(request)

    }

    fun reverseGeoAddress(context: Context, lat: Double, lng: Double) {
        try {
            val queue = RequestQueueSingleton.getInstance(context.applicationContext).requestQueue
            val request: StringRequest = object : StringRequest(
                Method.GET,
                Api.reverseGeo + "?key=" + BuildConfig.TRACE_API_KEY + "&latitude=" + lat + "&longitude=" + lng,
                Response.Listener { response: String? ->
                    try {
                        val data = JSONObject(response!!)
                        val place = JSONObject(data.getString("place"))
                        var address = ""
                        if (!place.getString("address").equals("null")) {
                            address = place.getString("address")
                        }
                        val city = place.getString("city")
                        val area = place.getString("area")

                        binding.tvLocation.text = "$address, $area, $city"

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

    private var startCamera = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        val filePath = sharePrefUtils.getString(ApiCall.IMAGE_PATH)
        if (result.resultCode == RESULT_CANCELED) {
            if (filePath != null) {
                Log.d("Image", "Canceled: $filePath")
                binding.attendanceImagePicker.deleteFileLocal(filePath)
                sharePrefUtils.saveString(ApiCall.IMAGE_PATH, "")
            }
        }
        if (result.resultCode == RESULT_OK) {
            Log.e("imageUtils", "OnActivity result code 1: $RESULT_OK")
            var imagePosition = 0
            val imageList =
                appDatabase.imagesDao()!!.getAllImageDB("Attendance") as ArrayList<Images?>?
            Log.d("Imagepos", "List: $imageList")
            imagePosition = if (imageList!!.isNotEmpty()) {
                imageList[imageList.size - 1]!!.position + 1
            } else {
                imagePosition + 1
            }
            binding.attendanceImagePicker.addNewImage(
                result.data,
                4,
                imagePosition,
                "Attendance",
                sharePrefUtils.getString(ApiCall.IMAGE_PATH)!!
            )
            try {
                val placeImage = Images(
                    null, imagePosition,
                    sharePrefUtils.getString(ApiCall.IMAGE_PATH)!!, "Attendance"
                )
                isImageAdded = true
                if (imagePosition > 0) {
                    Log.d("Imagepos", "insert")
                    Executors.newSingleThreadExecutor().execute {
                        appDatabase.imagesDao()!!.insertAll(placeImage)
                    }
                    sharePrefUtils.saveString(ApiCall.IMAGE_PATH, "")
                }
            } catch (e: Exception) {
                Log.e("imageUtils", "OnActivity result 2: $e")
                Sentry.captureException(e)
            }
        }
    }
}