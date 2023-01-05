package com.barikoi.cnlapp.Attendance.Fragment.TO

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Adapter.TO.HistoryListTOAdapter
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.Order_Create.Adapter.ShopSelectAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_create_attendance.*
import kotlinx.android.synthetic.main.fragment_history_t_o.*
import kotlinx.android.synthetic.main.fragment_shop_select.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryTOFragment : Fragment() {

    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id : String? = null
    var token : String? = null
    var historyList : ArrayList<HistoryList> = ArrayList()
    var adapter: HistoryListTOAdapter? = null
    var selected_so : Int? = null
    var selected_so_id : String? = null
    val soList: ArrayList<SOList> = ArrayList()
    val filteredsoList: ArrayList<HistoryList> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerSO.adapter.count >0) {
                    selected_so = p2
                    if (p2>0) {
                        selected_so_id = soList[p2 - 1].id
                    }
                    if (p2 == 0) {
                        setDateFilter("&with_to=1")
                    } else {
                        setDateFilter("")
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                    //setDateFilter()
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
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun init() {
        getSOList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_t_o, container, false)
    }

    fun setDateFilter(urlSuffix: String) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        editor!!.putString(Api.START_DATE_ATTENDANCE, StartDate)
        editor!!.putString(Api.END_DATE_ATTENDANCE, EndDate)
        editor!!.commit()

        getAttendance(Api.get_attendance+"?start_date="+StartDate+"&end_date="+EndDate+urlSuffix)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getAttendance(Api.get_attendance+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+urlSuffix)
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }
    }

    fun getAttendance(url: String){
        ApiServices.apiGET(
            url,
            mQueue!!, token!!, object : ApiServiceListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onResponseSuccess(response: String) {
                    getHistoryList(response)
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
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun viewSOList(response: String) {
        try {
            if (response != null){
                soList.clear()
                val obj = JSONObject(response)
                val soArray = obj.getJSONArray("so")
                val soNameList: ArrayList<String> = ArrayList()
                soNameList.add(prefs!!.getString(Api.NAME, "")+" (You)")
                if (soArray.length() >0){
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("user_name"),
                                soObj.getString("designation"),
                                soObj.getString("employee_id"),
                                soObj.getString("phone"),
                                soObj.getString("image_url")
                            )
                        )
                        soNameList.add(soObj.getString("user_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    mContext!!,
                    android.R.layout.simple_spinner_item, soNameList
                )
                spinnerSO.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getHistoryList(response: String){
        try {
            if (response != null){
                val obj = JSONObject(response)
                val attedanceArray = obj.getJSONArray("attendances")
                var latitude = 0.0
                var longitude = 0.0
                historyList.clear()
                if (attedanceArray.length() >0){
                    for (i in 0 until attedanceArray.length()) {
                        val attendanceObj = attedanceArray.getJSONObject(i)
                        if (!attendanceObj.getString("checkin_time").equals("null")) {
                            if (!attendanceObj.getString("latitude").equals("null")) latitude =
                                attendanceObj.getDouble("latitude")
                            if (!attendanceObj.getString("longitude").equals("null")) longitude =
                                attendanceObj.getDouble("longitude")
                            historyList.add(
                                HistoryList(
                                    attendanceObj.getString("user_name"),
                                    attendanceObj.getString("user_id"),
                                    attendanceObj.getString("id"),
                                    attendanceObj.getString("checkin_time"),
                                    attendanceObj.getString("checkout_time"),
                                    attendanceObj.getInt("is_late"),
                                    attendanceObj.getInt("is_absent"),
                                    attendanceObj.getString("checkin_address"),
                                    latitude,
                                    longitude,
                                    attendanceObj.getString("image_url"),
                                    attendanceObj.getString("late_reason"),
                                    attendanceObj.getString("route_id"),
                                    attendanceObj.getString("route_name")
                                )
                            )
                            /*if (attendanceObj.getInt("is_late") == 1) late += 1
                            if (attendanceObj.getInt("is_absent") == 1) absent += 1*/
                        }
                    }
                }

                if (selected_so == 0){
                    adapter = HistoryListTOAdapter(historyList)
                    historyTOListView.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }else{
                    filteredsoList.clear()
                    filteredsoList.addAll(historyList)
                    filteredsoList.removeIf {
                        !it.userId.equals(selected_so_id, true)
                    }
                    if (filteredsoList.size > 0){
                        adapter = HistoryListTOAdapter(filteredsoList)
                        historyTOListView.adapter = adapter
                        adapter!!.notifyDataSetChanged()
                    }
                }

                /*adapter = HistoryListTOAdapter(historyList)
                historyListView.adapter = adapter
                adapter!!.notifyDataSetChanged()*/
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        user_id = prefs!!.getString(Api.USER_ID, "")
        token = prefs!!.getString(Api.TOKEN, "")
        ACTIVITY = context as MainActivity
    }
}