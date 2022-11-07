package com.barikoi.cnlapp.Attendance.Fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.imagecapture.RoomDb.AppDatabase
import com.barikoi.cnlapp.imagecapture.RoomDb.Images
import com.barikoi.cnlapp.imagecapture.Utils.Api
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_create_attendance.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
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
    var appDatabase: AppDatabase? = null
    private var isImageAdded = false
    private val CAMERA = 4

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

        //setDateFilter()
        getAllRoutes(com.barikoi.cnlapp.Utils.Api.routes_withfilter+"?with_geometry=0&sr_id="+user_id)

        spinnerRoutes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val route_id = routeNameList!![p2].first
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        btnSubmit.setOnClickListener {
            submitAttendance()
        }
    }

    private fun submitAttendance() {

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
    /*private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        tvfilterDate.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        editor!!.putString(Api.STARTDATEAnnouncement, StartDate)
        editor!!.putString(Api.ENDDATEAnnouncement, EndDate)
        editor!!.commit()

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        filter_layout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            filter_layout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            filter_layout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvfilterDate.setText(simpleFormat.format(s_date))
                editor!!.putString(Api.STARTDATEAnnouncement, simpleFormat2.format(s_date))
                editor!!.putString(Api.ENDDATEAnnouncement, simpleFormat2.format(s_date))
                editor!!.commit()
            } else {
                tvfilterDate.setText(
                    simpleFormat.format(s_date) + " - " + simpleFormat.format(
                        e_date
                    )
                )
                editor!!.putString(Api.STARTDATEAnnouncement, simpleFormat2.format(s_date))
                editor!!.putString(Api.ENDDATEAnnouncement, simpleFormat2.format(e_date))
                editor!!.commit()
            }
            *//*StartDate = df.format(s_date)
            EndDate = df.format(e_date)
            val titles = arrayOf("All", "Individual")
            val fragments = java.util.ArrayList<Fragment>()
            fragments.add(FragmentAll())
            fragments.add(FragmentIndividual())
            viewPager.adapter = ViewPagerAdapter(parentFragmentManager, lifecycle, fragments)
            // attaching tab mediator
            TabLayoutMediator(
                tabLayout, viewPager
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }.attach()
            if (prefs!!.getInt(Api.ANNOUNCEMENT_PAGE_SELECTED, 0) == 0) {
                viewPager.currentItem = 0
            } else if (prefs!!.getInt(Api.ANNOUNCEMENT_PAGE_SELECTED, 0) == 1) {
                viewPager.currentItem = 1
            }
            viewPager.isUserInputEnabled = false*//*
        }

        materialDatePicker.addOnNegativeButtonClickListener { filter_layout.setEnabled(true) }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val filePath = prefs!!.getString(Api.IMAGE_PATH, "")

        if (resultCode == Activity.RESULT_CANCELED) {
            if (filePath != null) {
                //bottomSheetBehaviorinput.setState(BottomSheetBehavior.STATE_EXPANDED)
                Log.d("Image", "Canceled: $filePath")
                imagepicker.deleteFileLocal(filePath)
                editor!!.putString(Api.IMAGE_PATH, "")
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
                    prefs!!.getString(Api.IMAGE_PATH, "")!!
                )
                isImageAdded = true
                if (imagePosition > 0) {
                    Log.d("Imagepos", "insert")
                    Executors.newSingleThreadExecutor().execute {
                        appDatabase!!.imagesDao()!!.insertAll(placeImage)
                    }
                    editor!!.putString(Api.IMAGE_PATH, "")
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
        ACTIVITY = context as MainActivity
        appDatabase = AppDatabase.getInstance(mContext!!)
    }
}