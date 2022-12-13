package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Activity.ActiveInactiveActivity
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_order_summary_to.*
import kotlinx.android.synthetic.main.fragment_home_t_o.*
import kotlinx.android.synthetic.main.fragment_home_t_o.bpcCount
import kotlinx.android.synthetic.main.fragment_home_t_o.lpcCount
import kotlinx.android.synthetic.main.fragment_home_t_o.ovCount
import kotlinx.android.synthetic.main.fragment_home_t_o.tvDateRange
import kotlinx.android.synthetic.main.fragment_shop_select.*
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class HomeTOFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String ? = ""
    var territoryId: String ? = ""
    var employeeId: String? = ""

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
        //checkforAttendanceToday()

        init()

        liveStockUpdate.setOnClickListener {
            startActivity(Intent(requireActivity(), ProductStockUpdateActivity::class.java))
        }

        lastweeksummary.setOnClickListener {
            startActivity(Intent(requireActivity(), OrderSummaryTOActivity::class.java).putExtra("from", "lastweek"))
        }

        tryAgain.setOnClickListener {
            setLastWeekSummary()
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
                                /*if (!attendanceObj.getString("route_id").equals("null")) {
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
                                    //MainActivity.routeName_selected!!.setText(attendanceObj.getString("route_name"))
                                    //routeId =  attendanceObj.getInt("route_id").toString()
                                    init()
                                }else{
                                    no_route_check.visibility = View.VISIBLE
                                    bodyLayout.visibility = View.GONE

                                    btn_tryAgain.setOnClickListener {
                                        checkforAttendanceToday()
                                    }
                                }*/
                                init()
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

            getSummaryTargets(Api.get_summary+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+"&with_to_stats=1&territory_id="+territoryId)
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayoutHome.setEnabled(true) }

        getSummaryTargets(Api.get_summary+"?start_date="+StartDate+"&end_date="+EndDate+"&with_to_stats=1&territory_id="+territoryId)

    }

    private fun getSummaryTargets(url: String) {
        var total_target = "--:--"
        var total_target_completed = "--:--"
        var target_ads = "--:--"
        var ads_completed = "--:--"
        var target_rds = "--:--"
        var rds_completed = "--:--"
        var number_of_memo = "--:--"
        var number_of_memo_completed = "--:--"
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        no_route_check.visibility = View.GONE
                        bodyLayout.visibility = View.VISIBLE
                        if (targetsArray.length() > 0){
                            for (i in 0 until targetsArray.length()){
                                val targetObj =targetsArray.getJSONObject(i)
                                if(!targetObj.isNull("target_amount"))total_target = Math.round(targetObj.getString("target_amount").toDouble()).toString()
                                if(!targetObj.isNull("target_ads"))target_ads = dformat.format(targetObj.getString("target_ads").toDouble())
                                if(!targetObj.isNull("target_rds"))target_rds = dformat.format(targetObj.getString("target_rds").toDouble())
                                if(!targetObj.isNull("target_number_of_memo"))number_of_memo = dformat.format(targetObj.getString("target_number_of_memo").toDouble())
                            }
                        }
                        if (completedArray.length() > 0){
                            for (i in 0 until completedArray.length()){
                                val targetObj =completedArray.getJSONObject(i)
                                if(!targetObj.isNull("revenue")) total_target_completed = Math.round(targetObj.getString("revenue").toDouble()).toString()
                                if(!targetObj.isNull("ads"))ads_completed = dformat.format(targetObj.getString("ads").toDouble())
                                if(!targetObj.isNull("rds"))rds_completed = dformat.format(targetObj.getString("rds").toDouble())
                                if(!targetObj.isNull("number_of_memo"))number_of_memo_completed = dformat.format(targetObj.getString("number_of_memo").toDouble())
                            }
                        }

                        val itemList: ArrayList<TargetValue> = ArrayList()
                        itemList.add(TargetValue(resources.getString(R.string.total_target), total_target, total_target_completed))
                        itemList.add(TargetValue(resources.getString(R.string.ads), target_ads, ads_completed))
                        itemList.add(TargetValue(resources.getString(R.string.rds), target_rds, rds_completed))
                        itemList.add(TargetValue(resources.getString(R.string.number_of_memo), number_of_memo, number_of_memo_completed))

                        val adapter = TargetAdapter(itemList, "TO")
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
        val gd = GradientDrawable()
        gd.setColor(mContext!!.resources.getColor(R.color.white))
        gd.cornerRadius = 16f
        gd.setStroke(3, mContext!!.resources.getColor(R.color.cnl_color_2))
        activeLayout.setBackgroundDrawable(gd)
        val gd2 = GradientDrawable()
        gd2.setColor(mContext!!.resources.getColor(R.color.white))
        gd2.cornerRadius = 16f
        gd2.setStroke(3, mContext!!.resources.getColor(R.color.cnl_color_1))
        inactiveLayout.setBackgroundDrawable(gd2)

        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ApiServices.apiGET(
            Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today)+"&with_active_inactive_so=1", mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        //val attendanceArray = obj.getJSONArray("active")
                        val activeSO = obj.getJSONArray("active").length()
                        val inactiveSO = obj.getJSONArray("inactive").length()
                        activeCount.setText(activeSO.toString())
                        inactiveCount.setText(inactiveSO.toString())
                        if (obj.getJSONArray("active").length() > 0){
                            activeLayout.setOnClickListener {
                                startActivity(Intent(requireActivity(), ActiveInactiveActivity::class.java).putExtra("so_status", "active"))
                            }
                        }
                        if (obj.getJSONArray("inactive").length() > 0){
                            inactiveLayout.setOnClickListener {
                                startActivity(Intent(requireActivity(), ActiveInactiveActivity::class.java).putExtra("so_status", "inactive"))
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
                TODO("Not yet implemented")
            }

        })
    }
    private fun setLiveStockView() {
        val itemList: ArrayList<Pair<String, String>> = ArrayList()
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ApiServices.apiGET(Api.all_product_list+"?start_date="+today+" 00:00:00"+"&end_date="+today+" 23:59:59"+"&with_stock=1&territory_id="+territoryId, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")
                        itemList.clear()
                        if (productsArray.length() > 0) {
                            for (i in 0 until productsArray.length()) {
                                val productObj = productsArray.getJSONObject(i)
                                itemList.add(
                                    Pair(
                                        productObj.getString("product_name"),
                                        productObj.getString("current_available_stock")
                                    )
                                )
                            }

                            itemList.sortBy {
                                it.second
                            }
                            createTable(itemList, tabLayout)
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
    private fun setLastWeekSummary() {
        try{
        progressBarHome.visibility = View.VISIBLE
        val dformat = DecimalFormat("#.##")
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("LLL dd", Locale.getDefault())
        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        val StartDate = df.format(start)
        val EndDate = df.format(start)
        ApiServices.apiGET(Api.get_all_so_list+"?last_week_summary=1&start_date="+StartDate+" 00:00:00"+"&end_date="+EndDate+" 23:59:59"+"&to="+employeeId, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        progressBarHome.visibility = View.GONE
                        summaryLayout.visibility = View.VISIBLE
                        tryAgain.visibility = View.GONE
                        val obj = JSONObject(response)
                        val ordersArray = obj.getJSONArray("so_list")
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        if (ordersArray.length() >0){
                            for(i in 0 until ordersArray.length()){
                                val orderObj = ordersArray.getJSONObject(i)
                                itemList.add(Pair(orderObj.getString("sr_name"), dformat.format(orderObj.getString("so_ordered_value").toDouble())))
                            }
                        }
                        if (!obj.getString("order_amount").equals("null")) ovCount.setText(dformat.format(obj.getString("order_amount").toDouble()))
                        if (!obj.getString("sku_per_memo").equals("null")) bpcCount.setText(dformat.format(obj.getString("sku_per_memo").toDouble()))
                        if (!obj.getString("number_of_memo").equals("null")) lpcCount.setText(dformat.format(obj.getString("number_of_memo").toDouble()))
                        createTable(itemList, tabLayout2)

                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    progressBarHome.visibility = View.GONE
                    summaryLayout.visibility = View.GONE
                    tryAgain.visibility = View.VISIBLE
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                try{
                ViewUtils.getErrorResponse(error, mContext!!)
                progressBarHome.visibility = View.GONE
                summaryLayout.visibility = View.GONE
                tryAgain.visibility = View.VISIBLE
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onException(e: Exception) {
                try{
                progressBarHome.visibility = View.GONE
                summaryLayout.visibility = View.GONE
                tryAgain.visibility = View.VISIBLE
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

        })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        var size : Int = 0
        if (data.size<5){
            size = data.size
        }else{
            size = 5
        }
        for (i in 0 until size) {
            val tr = TableRow(mContext)
            val tableRowParams = TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.setLayoutParams(tableRowParams)
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(mContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(mContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.gravity = Gravity.CENTER
            c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tab_Layout.addView(tr)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        territoryId = prefs!!.getString(Api.TERRITORY_ID, "")
    }



}