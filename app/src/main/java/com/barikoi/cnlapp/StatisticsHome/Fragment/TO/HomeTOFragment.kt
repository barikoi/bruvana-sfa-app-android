package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_home_t_o.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeTOFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    val dots: ArrayList<ImageView> = ArrayList()
    var token: String ? = ""
    var srId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_t_o, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkforAttendanceToday()

        liveStockUpdate.setOnClickListener {
            startActivity(Intent(requireActivity(), ProductStockUpdateActivity::class.java))
        }

        lastweeksummary.setOnClickListener {
            startActivity(Intent(requireActivity(), OrderSummaryTOActivity::class.java))
        }

    }
    private fun checkforAttendanceToday() {
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ApiServices.apiGET(
            Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today),
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null) {
                            val obj = JSONObject(response)
                            val attedanceArray = obj.getJSONArray("attendances")
                            if (attedanceArray.length() >0){
                                no_route_check.visibility = View.GONE
                                bodyLayout.visibility = View.VISIBLE
                                val attendanceObj = attedanceArray.getJSONObject(0)
                                if (!attendanceObj.getString("route_id").equals("null")) {
                                    attendanceObj.getInt("route_id")
                                    attendanceObj.getString("route_name")
                                    editor!!.putString(
                                        Api.SELECTED_ROUTE_ID,
                                        attendanceObj.getInt("route_id").toString()
                                    )
                                        .putString(
                                            Api.SELECTED_ROUTE_NAME,
                                            attendanceObj.getString("route_name")
                                        ).commit()
                                    MainActivity.routeName_selected!!.setText(attendanceObj.getString("route_name"))
                                    routeId =  attendanceObj.getInt("route_id").toString()
                                    init()
                                }else{
                                    no_route_check.visibility = View.VISIBLE
                                    bodyLayout.visibility = View.GONE

                                    btn_tryAgain.setOnClickListener {
                                        checkforAttendanceToday()
                                    }
                                }

                            }else{
                                no_route_check.visibility = View.VISIBLE
                                bodyLayout.visibility = View.GONE

                                btn_tryAgain.setOnClickListener {
                                    checkforAttendanceToday()
                                }
                            }
                        }
                    }catch (e: Exception){
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
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun init() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("LLL dd", Locale.getDefault())
        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayoutHome.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayoutHome.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayoutHome.setEnabled(true)
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

            getSummaryTargets(Api.get_summary+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+"&sr_id="+srId+"&route_id="+routeId)
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayoutHome.setEnabled(true) }

        getSummaryTargets(Api.get_summary+"?start_date="+StartDate+"&end_date="+EndDate+"&sr_id="+srId+"&route_id="+routeId)

    }

    private fun getSummaryTargets(url: String) {
        var total_target = ""
        var total_target_completed = ""
        var lpc = ""
        var lpc_completed = ""
        var bpc = ""
        var bpc_completed = ""
        var aiv = ""
        var aiv_completed = ""
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        if (targetsArray.length() > 0){
                            for (i in 0 until targetsArray.length()){
                                val targetObj =targetsArray.getJSONObject(i)
                                total_target = Math.round(targetObj.getString("target_amount").toDouble()).toString()
                                bpc = dformat.format(targetObj.getString("target_sku_per_memo").toDouble())
                                lpc = dformat.format(targetObj.getString("target_number_of_memo").toDouble())
                                aiv = dformat.format(targetObj.getString("target_aiv").toDouble())
                            }
                        }
                        if (completedArray.length() > 0){
                            for (i in 0 until completedArray.length()){
                                val targetObj =completedArray.getJSONObject(i)
                                total_target_completed = Math.round(targetObj.getString("revenue").toDouble()).toString()
                                bpc_completed = dformat.format(targetObj.getString("bpc").toDouble())
                                lpc_completed = dformat.format(targetObj.getString("lpc").toDouble())
                                aiv_completed = dformat.format(targetObj.getString("aiv").toDouble())
                            }
                        }

                        val itemList: ArrayList<TargetValue> = ArrayList()
                        itemList.add(TargetValue(resources.getString(R.string.total_target), total_target, total_target_completed))
                        itemList.add(TargetValue(resources.getString(R.string.sku_per_memo), bpc, bpc_completed))
                        itemList.add(TargetValue(resources.getString(R.string.number_of_memo), lpc, lpc_completed))
                        itemList.add(TargetValue(resources.getString(R.string.aiv), aiv, aiv_completed))

                        val adapter = TargetAdapter(itemList, "SO")
                        targetListView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        setActiveInactiveView()
                        setLiveStockView()
                        setLastWeekSummary()

                    }
                }catch (e: Exception){
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
                TODO("Not yet implemented")
            }

        })

    }
    private fun setActiveInactiveView() {
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ApiServices.apiGET(
            Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today)+"&with_active_inactive_so=1", mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val attendanceArray = obj.getJSONArray("attendances")
                        if (attendanceArray.length() > 0){
                            val obj = attendanceArray.getJSONObject(0)
                            val activeSO = obj.getString("active_so")
                            val inactiveSO = obj.getString("inactive_so")

                            if (!activeSO.equals("null")) activeCount.setText(activeSO)
                            if (!inactiveSO.equals("null")) inactiveCount.setText(inactiveSO)
                        }


                    }
                }catch (e: Exception){
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
                TODO("Not yet implemented")
            }

        })
    }
    private fun setLiveStockView() {
        ApiServices.apiGET(Api.get_all_so_list, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        /*itemList.add(Pair(resources.getString(R.string.total_order_value), total_target_completed))
                        itemList.add(Pair(resources.getString(R.string.sku_per_memo), bpc_completed))
                        itemList.add(Pair(resources.getString(R.string.visit_ratio), visit_ratio+"%"))
                        itemList.add(Pair(resources.getString(R.string.number_of_memo), lpc_completed))
                        itemList.add(Pair(resources.getString(R.string.aiv), aiv_completed))*/
                        createTable(itemList, tabLayout)

                    }
                }catch (e: Exception){
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
                TODO("Not yet implemented")
            }

        })
    }
    private fun setLastWeekSummary() {
        ApiServices.apiGET(Api.get_all_so_list, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        /*itemList.add(Pair(resources.getString(R.string.total_order_value), total_target_completed))
                        itemList.add(Pair(resources.getString(R.string.sku_per_memo), bpc_completed))
                        itemList.add(Pair(resources.getString(R.string.visit_ratio), visit_ratio+"%"))
                        itemList.add(Pair(resources.getString(R.string.number_of_memo), lpc_completed))
                        itemList.add(Pair(resources.getString(R.string.aiv), aiv_completed))*/
                        createTable(itemList, tabLayout2)

                    }
                }catch (e: Exception){
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
                TODO("Not yet implemented")
            }

        })
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout2: TableLayout) {
        tabLayout2.isStretchAllColumns = true
        tabLayout2.bringToFront()
        for (i in 0 until data.size) {
            val tr = TableRow(mContext)
            val c1 = TextView(mContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(mContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tabLayout.addView(tr)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        srId = prefs!!.getString(Api.SR_CODE, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }



}