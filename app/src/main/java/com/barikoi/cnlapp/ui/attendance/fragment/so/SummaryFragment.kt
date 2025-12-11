package com.barikoi.cnlapp.ui.attendance.fragment.so

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentSummaryBinding
import com.barikoi.cnlapp.ui.attendance.adapter.so.ReasonListAdapter
import com.barikoi.cnlapp.ui.main.MainActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class SummaryFragment : Fragment() {
    private lateinit var binding: FragmentSummaryBinding

    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id: String? = null
    var token: String? = null
    var reasonList: ArrayList<Pair<String, String>> = ArrayList()


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
        binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        setDateFilter()
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))

        editor!!.putString(Api.START_DATE_ATTENDANCE, StartDate)
        editor!!.putString(Api.END_DATE_ATTENDANCE, EndDate)
        editor!!.commit()

        getSummaryList(Api.get_attendance + "?start_date=" + StartDate + "&end_date=" + EndDate)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding. dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            binding. dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                binding. tvDateRange.text = simpleFormat.format(s_date)
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()
            } else {

                binding.tvDateRange.text =
                    getString(
                        R.string.date_range_,
                        simpleFormat.format(s_date),
                        simpleFormat.format(e_date)
                    )
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getSummaryList(
                Api.get_attendance + "?start_date=" + df.format(s_date) + "&end_date=" + df.format(
                    e_date
                )
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener { binding.dateRangeLayout.setEnabled(true) }
    }

    private fun getSummaryList(url: String) {
        ApiServices.apiGET(
            url,
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

    fun getHistoryList(response: String) {
        try {
            if (response != null) {
                val obj = JSONObject(response)
                val attedanceArray = obj.getJSONArray("attendances")
                var absent = 0
                var present = 0
                var late = 0
                reasonList.clear()
                if (attedanceArray.length() > 0) {
                    for (i in 0 until attedanceArray.length()) {
                        val attendanceObj = attedanceArray.getJSONObject(i)
                        if (!attendanceObj.getString("remarks")
                                .equals("null") && attendanceObj.getString("remarks").length > 0
                        ) {
                            reasonList.add(
                                Pair(
                                    attendanceObj.getString("checkin_time"),
                                    attendanceObj.getString("remarks")
                                )
                            )
                        }

                        if (attendanceObj.getInt("is_late") == 1) late += 1
                        if (attendanceObj.getInt("is_absent") == 1) absent += 1
                    }

                    present = attedanceArray.length() - absent

                    editor!!.putInt(Api.TOTAL_PRESENT, present)
                    editor!!.putInt(Api.TOTAL_LATE, late)
                    editor!!.putInt(Api.TOTAL_ABSENT, absent)
                    editor!!.commit()

                    /*if (reasonList.size > 0){
                        val adapter = ReasonListAdapter(reasonList)
                        summaryListView.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }*/
                }

                val adapter = ReasonListAdapter(reasonList)
                binding.summaryListView.adapter = adapter
                adapter.notifyDataSetChanged()

                binding.presentCount.setText(present.toString())
                binding.lateCount.setText(late.toString())
                binding.absentCount.setText(absent.toString())
            }
        } catch (e: Exception) {
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