package com.barikoi.cnlapp.Attendance.Fragment.TO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Adapter.TO.HistoryListTOAdapter
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_history_t_o.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getSOList()
        init()
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            mQueue!!, token!!, object : ApiServiceListener {
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

    private fun init() {
        setDateFilter()

        adapter = HistoryListTOAdapter(historyList)
        historyListView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_t_o, container, false)
    }

    fun setDateFilter() {
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

        getAttendance(Api.get_attendance+"?start_date="+StartDate+"&end_date="+EndDate)

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

            getAttendance(Api.get_attendance+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date))
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }
    }

    fun getAttendance(url: String){
        ApiServices.apiGET(
            url,
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
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
                val obj = JSONObject(response)
                val soArray = obj.getJSONArray("so")
                if (soArray.length() >0){
                    val soList: ArrayList<SOList> = ArrayList()
                    val soNameList: ArrayList<String> = ArrayList()
                    soNameList.add(prefs!!.getString(Api.NAME, "")+" (You)")
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("name"),
                                soObj.getString("designation"),
                                soObj.getString("employee_id"),
                                soObj.getString("phone"),
                                soObj.getString("image")
                            )
                        )
                        soNameList.add(soObj.getString("name"))

                    }
                }

                adapter = HistoryListTOAdapter(historyList)
                historyListView.adapter = adapter
                adapter!!.notifyDataSetChanged()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

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
                        if (!attendanceObj.getString("enter_time").equals("null")) {
                            if (!attendanceObj.getString("latitude").equals("null")) latitude =
                                attendanceObj.getDouble("latitude")
                            if (!attendanceObj.getString("longitude").equals("null")) longitude =
                                attendanceObj.getDouble("longitude")
                            historyList.add(
                                HistoryList(
                                    attendanceObj.getString("name"),
                                    attendanceObj.getString("id"),
                                    attendanceObj.getString("enter_time"),
                                    attendanceObj.getString("exit_time"),
                                    attendanceObj.getInt("is_late"),
                                    attendanceObj.getInt("is_absent"),
                                    attendanceObj.getString("checkin_address"),
                                    latitude,
                                    longitude,
                                    attendanceObj.getString("image"),
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

                adapter = HistoryListTOAdapter(historyList)
                historyListView.adapter = adapter
                adapter!!.notifyDataSetChanged()
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