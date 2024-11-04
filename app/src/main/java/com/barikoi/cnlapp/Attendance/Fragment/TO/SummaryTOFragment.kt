package com.barikoi.cnlapp.Attendance.Fragment.TO

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Attendance.Adapter.SO.ReasonListAdapter
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentSummaryTOBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class SummaryTOFragment : Fragment() {
    private lateinit var binding: FragmentSummaryTOBinding

    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var user_id: String? = null
    var token: String? = null
    var adapter: ReasonListAdapter? = null
    var selected_so: Int? = null
    var selected_so_id: String? = null
    val soList: ArrayList<SOList> = ArrayList()
    val filteredsoList: ArrayList<Pair<String, String>> = ArrayList()
    var reasonList: ArrayList<Pair<String, String>> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO.adapter.count > 0) {
                    selected_so = p2
                    if (p2 > 0) {
                        binding.userLayout.visibility = View.VISIBLE
                        selected_so_id = soList[p2 - 1].id
                        if (!soList[p2 - 1].imageUrl.equals("null")) {
                            Glide.with(mContext!!)
                                .load(soList[p2 - 1].imageUrl)
                                .into(binding.imageUser)
                        } else {
                            binding.imageUser.visibility = View.GONE
                        }
                        binding.userName.setText(soList[p2 - 1].name)
                        binding.userDesignation.setText(soList[p2 - 1].designation)
                    } else {
                        binding.userLayout.visibility = View.GONE
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSummaryTOBinding.inflate(inflater, container, false)
        return  binding.root
    }

    private fun init() {
        getSOList()
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

    private fun viewSOList(response: String) {
        try {
            if (response != null) {
                soList.clear()
                val obj = JSONObject(response)
                val toArray = obj.getJSONArray("so_list")
                val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
                val soNameList: ArrayList<String> = ArrayList()
                soNameList.add(prefs!!.getString(Api.NAME, "") + " (You)")

                if (soArray.length() > 0) {
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        var imageUrl = "null"
                        if (soObj.has("images")  && !soObj.isNull("images")){
                            val imageArray = soObj.getJSONArray("images")
                            if (imageArray.length() > 0){
                                val imageobj = imageArray.getJSONObject(0)
                                if (imageobj.has("image_url")){
                                    imageUrl = imageobj.getString("image_url")
                                }
                            }
                        }
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("user_name"),
                                soObj.getString("designation"),
                                if(soObj.has("employee_id")) soObj.getString("employee_id") else "",
                                imageUrl
                            )
                        )
                        soNameList.add(soObj.getString("user_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    mContext!!,
                    android.R.layout.simple_spinner_item, soNameList
                )
                binding. spinnerSO.adapter = adapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDateFilter(urlSuffix: String) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        binding.tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        editor!!.putString(Api.START_DATE_ATTENDANCE, StartDate)
        editor!!.putString(Api.END_DATE_ATTENDANCE, EndDate)
        editor!!.commit()

        getAttendance(Api.get_attendance + "?start_date=" + StartDate + "&end_date=" + EndDate + urlSuffix)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                binding. tvDateRange.setText(simpleFormat.format(s_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()
            } else {
                binding.tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getAttendance(
                Api.get_attendance + "?start_date=" + df.format(s_date) + "&end_date=" + df.format(
                    e_date
                ) + urlSuffix
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener { binding.dateRangeLayout.setEnabled(true) }
    }

    fun getAttendance(url: String) {
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

    fun getHistoryList(response: String) {
        try {
            if (response != null) {
                val obj = JSONObject(response)
                val attedanceArray = obj.getJSONArray("attendances")
                var absent = 0
                var present = 0
                var late = 0
                var total = 0
                reasonList.clear()
                if (attedanceArray.length() > 0) {
                    for (i in 0 until attedanceArray.length()) {
                        val attendanceObj = attedanceArray.getJSONObject(i)
                        if (attendanceObj.getString("user_id").equals(selected_so_id)) {
                            total +=1
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
                            if (attendanceObj.getString("checkin_time").equals("null") && attendanceObj.getInt("is_absent") == 0) {
                                total -= 1
                            }
                            if (attendanceObj.getInt("is_late") == 1) late += 1
                            if (attendanceObj.getInt("is_absent") == 1) absent += 1
                        }else{
                            if (selected_so == 0){
                                total +=1
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
                                if (attendanceObj.getString("checkin_time").equals("null") && attendanceObj.getInt("is_absent") == 0) {
                                    total -= 1
                                }
                                if (attendanceObj.getInt("is_late") == 1) late += 1
                                if (attendanceObj.getInt("is_absent") == 1) absent += 1
                            }
                        }
                    }

                    present = total - absent

                    editor!!.putInt(Api.TOTAL_PRESENT, present)
                    editor!!.putInt(Api.TOTAL_LATE, late)
                    editor!!.putInt(Api.TOTAL_ABSENT, absent)
                    editor!!.commit()
                }

                val adapter = ReasonListAdapter(reasonList)
                binding.summaryListTOView.adapter = adapter
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