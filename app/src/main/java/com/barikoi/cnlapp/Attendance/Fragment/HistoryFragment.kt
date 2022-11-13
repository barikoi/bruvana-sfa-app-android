package com.barikoi.cnlapp.Attendance.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.*
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Adapter.HistoryListAdapter
import com.barikoi.cnlapp.Attendance.Model.HistoryList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_history.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistoryFragment : Fragment() {

    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id : String? = null
    var token : String? = null
    var historyList : ArrayList<HistoryList> = ArrayList()
    var adapter: HistoryListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setDateFilter()

        adapter = HistoryListAdapter(historyList)
        historyListView.adapter = adapter
        //adapter!!.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    private fun setDateFilter() {
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

        ApiServices.apiGET(Api.get_attendance+"?start_date="+StartDate+"&end_date="+EndDate,
            mQueue!!, token!!, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    getHistoryList(response)
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

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
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

            ApiServices.apiGET(Api.get_attendance+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date),
                mQueue!!, token!!, object : ApiServiceListener{
                    override fun onResponseSuccess(response: String) {
                        getHistoryList(response)
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

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }
    }

    fun getHistoryList(response: String){
        try {
            if (response != null){
                val obj = JSONObject(response)
                val attedanceArray = obj.getJSONArray("attendances")
                var absent = 0
                var present = 0
                var late= 0
                historyList.clear()
                if (attedanceArray.length() >0){
                    for (i in 0 until attedanceArray.length()) {
                        val attendanceObj = attedanceArray.getJSONObject(i)
                        historyList.add(
                            HistoryList(
                                attendanceObj.getString("id"),
                                attendanceObj.getString("enter_time"),
                                attendanceObj.getString("exit_time"),
                                attendanceObj.getInt("is_late"),
                                attendanceObj.getInt("is_absent"),
                                attendanceObj.getString("checkin_address"),
                                attendanceObj.getDouble("latitude"),
                                attendanceObj.getDouble("longitude"),
                                attendanceObj.getString("image"),
                                attendanceObj.getString("late_reason"),
                                attendanceObj.getInt("route_id"),
                                attendanceObj.getString("route_name")
                        )
                        )
                        if (attendanceObj.getInt("is_late") == 1) late += 1
                        if (attendanceObj.getInt("is_absent") == 1) absent +=1
                    }

                    present = attedanceArray.length() - absent

                    /*editor!!.putInt(Api.TOTAL_PRESENT, present)
                    editor!!.putInt(Api.TOTAL_LATE, late)
                    editor!!.putInt(Api.TOTAL_ABSENT, absent)
                    editor!!.commit()*/

                    /*if (historyList.size > 0){
                        adapter = HistoryListAdapter(historyList)
                        historyListView.adapter = adapter
                        adapter!!.notifyDataSetChanged()
                    }*/
                }

                adapter = HistoryListAdapter(historyList)
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