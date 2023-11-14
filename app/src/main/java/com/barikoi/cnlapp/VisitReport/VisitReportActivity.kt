package com.barikoi.cnlapp.VisitReport

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_visit_report.*
import kotlinx.android.synthetic.main.activity_visit_report.dateRangeLayout
import kotlinx.android.synthetic.main.activity_visit_report.spinnerLayout
import kotlinx.android.synthetic.main.activity_visit_report.spinnerSO
import kotlinx.android.synthetic.main.activity_visit_report.tabLayout
import kotlinx.android.synthetic.main.activity_visit_report.tvDateRange
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VisitReportActivity : AppCompatActivity() {
    var token: String? = null
    var user_id: String? = null
    var route_id: String? = null
    var sr_id: String? = null
    var employeeId: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var StartDate: String? = null
    var EndDate: String? = null
    var customDate: String? = null
    val soList: ArrayList<SOList> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_report)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO")){
            spinnerLayout.visibility = View.VISIBLE
            getSOList()
        }else{
            spinnerLayout.visibility = View.GONE
            sr_id = user_id
        }

        val menuList = arrayOf(
            resources.getString(R.string.today_report),
            resources.getString(R.string.last_week_report),
            resources.getString(R.string.custom)
        )
        val adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item, menuList
        )
        spinnerMenu.adapter = adapter
        spinnerMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerMenu.adapter.count > 0) {
                    tabLayout.visibility = View.GONE
                    tabLayout2.visibility = View.GONE
                    if (p2 == 2) {
                        tvDateRange.setText("Select date range")
                    } else {
                        setDateFilter(p2)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerSO.adapter.count >0) {
                    sr_id = soList[p2].id
                    tabLayout.visibility = View.GONE
                    tabLayout2.visibility = View.GONE
                    getVisitReports(sr_id!!, StartDate!!, EndDate!!)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        setDateFilter(spinnerMenu.selectedItemPosition)
    }

    private fun setDateFilter(position: Int) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        if (position == 0) {
            StartDate = df.format(end)
            EndDate = df.format(end)
            tvDateRange.setText(simpleFormat.format(end))
            if (sr_id != null) {
                tabLayout.visibility = View.GONE
                tabLayout2.visibility = View.GONE
                getVisitReports(sr_id!!, StartDate!!, EndDate!!)
            }
        } else if (position == 1) {
            StartDate = df.format(start)
            EndDate = df.format(start)
            tvDateRange.setText(simpleFormat.format(start))
            if (sr_id != null) {
                tabLayout.visibility = View.GONE
                tabLayout2.visibility = View.GONE
                getVisitReports(sr_id!!, StartDate!!, EndDate!!)
            }
        } else {
            tvDateRange.setText("Select date range")
            tvDateRange.setText(customDate)
        }

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            spinnerMenu.setSelection(2)
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            StartDate = df.format(s_date)
            EndDate = df.format(e_date)
            //spinnerMenu.setSelection(2)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                customDate = simpleFormat.format(s_date)
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                customDate = simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date)
            }

            if (sr_id != null) {
                tabLayout.visibility = View.GONE
                tabLayout2.visibility = View.GONE
                getVisitReports(sr_id!!, StartDate!!, EndDate!!)
            }
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

    }


    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            queue!!, token!!, object : ApiServiceListener {
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
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun viewSOList(response: String) {
        try {
            if (response != null){
                soList.clear()
                val obj = JSONObject(response)
                val toArray = obj.getJSONArray("so_list")
                val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
                val soNameList: ArrayList<String> = ArrayList()
                var imageUrl = "null"
                if (soArray.length() >0){
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
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
                    applicationContext,
                    android.R.layout.simple_spinner_item, soNameList
                )
                spinnerSO.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getVisitReports(sr_id: String, startDate: String, endDate: String) {
        var url = ""
        if (prefs!!.getString(Api.USER_TYPE, "").equals("SO", true)){
            url = Api.get_visit_report+"?user_id="+sr_id+"&start_date="+startDate+"&end_date="+endDate
        }else{
            url = Api.get_visit_report+"?user_id="+sr_id+"&start_date="+startDate+"&end_date="+endDate
        }
        progressBar.visibility = View.VISIBLE
        ApiServices.apiGET(
            url,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        progressBar.visibility = View.GONE
                        if (response != null){
                            val obj = JSONObject(response)
                            val productsArray = obj.getJSONArray("visited_report")
                            val itemList : ArrayList<Pair<String, String>> = ArrayList()
                            if (productsArray.length() > 0) {
                                for (i in 0 until productsArray.length()) {
                                    val productObj = productsArray.getJSONObject(i)
                                    itemList.add(
                                        Pair(
                                            productObj.getString("range"),
                                            productObj.getString("visited_count")
                                        )
                                    )
                                }
                                createTable(itemList, tabLayout)
                            }

                            if (obj.has("total_visited_report") && !obj.isNull("total_visited_report")){
                                val visitedArray = obj.getJSONArray("total_visited_report")
                                val visitedList : ArrayList<Pair<String, String>> = ArrayList()
                                if (visitedArray.length()>0){
                                    for (i in 0 until visitedArray.length()) {
                                        val visitedObj = visitedArray.getJSONObject(i)
                                        val keys = visitedObj.keys()
                                        while(keys.hasNext()){
                                            val key = keys.next() as String
                                            visitedList.add(
                                                Pair(
                                                    key,
                                                    visitedObj.getString(key)
                                                )
                                            )
                                        }

                                    }
                                    createTableOther(visitedList, tabLayout2)
                                }
                            }

                        }
                    }catch (e: Exception){
                        progressBar.visibility= View.GONE
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
                    progressBar.visibility= View.GONE
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    progressBar.visibility= View.GONE
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        tab_Layout.visibility = View.VISIBLE

        for (i in 0 until data.size) {
            val tr = TableRow(applicationContext)
            val tableRowParams = TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.setLayoutParams(tableRowParams)
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(applicationContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(applicationContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.gravity = Gravity.CENTER
            c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tab_Layout.addView(tr)
            tab_Layout.background = resources.getDrawable(R.drawable.cardview_bg_stroke_2dp)
        }
    }

    private fun createTableOther(data: ArrayList<Pair<String, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        tab_Layout.visibility = View.VISIBLE

        for (i in 0 until data.size) {
            val tr = TableRow(applicationContext)
            val tableRowParams = TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.setLayoutParams(tableRowParams)
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(applicationContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(applicationContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.gravity = Gravity.CENTER
            //c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tab_Layout.addView(tr)
            tab_Layout.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
        }
    }
}