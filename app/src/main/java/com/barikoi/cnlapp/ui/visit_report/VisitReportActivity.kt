package com.barikoi.cnlapp.ui.visit_report

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityVisitReportBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.formatDateToFullName
import com.barikoi.cnlapp.utils.extension.formatFullMonthDateYear
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class VisitReportActivity : BaseActivity() {
    private lateinit var binding: ActivityVisitReportBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var queue: RequestQueue

    var srId: String? = null
    var startDate: String? = null
    var endDate: String? = null
    private var customDate: String? = null
    val soList: ArrayList<SOList> = ArrayList()


    var isCustomDate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVisitReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO") ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")
        ) {
            binding.spinnerLayoutRoute.isVisible = true
            getSOList()
        } else {
            binding.spinnerLayoutRoute.isVisible = false
            srId = sharePrefUtils.getString(Api.USER_ID)
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
        binding.spinnerMenu.adapter = adapter
        binding.spinnerMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerMenu.adapter.count > 0) {
                    binding.tabLayout.visibility = View.GONE
                    binding.tabLayout2.visibility = View.GONE
                    if (p2 == 2) {
                        if (!isCustomDate)
                            binding.tvDateRange.text = getString(R.string.select_date_range)
                    } else {
                        setDateFilter(p2)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        binding.spinnerSO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (binding.spinnerSO.adapter.count > 0) {
                    srId = soList[p2].id
                    binding.tabLayout.visibility = View.GONE
                    binding.tabLayout2.visibility = View.GONE
                    getVisitReports(srId!!, startDate!!, endDate!!)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        setDateFilter(binding.spinnerMenu.selectedItemPosition)
    }

    private fun setDateFilter(position: Int) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        if (position == 0) {
            startDate = end.formatDate()
            endDate = end.formatDate()
            binding.tvDateRange.text = end.formatDateToFullName()
            if (srId != null) {
                binding.tabLayout.visibility = View.GONE
                binding.tabLayout2.visibility = View.GONE
                getVisitReports(srId!!, startDate!!, endDate!!)
            }
        } else if (position == 1) {
            startDate = df.format(start)
            endDate = df.format(start)
            binding.tvDateRange.text = start.formatFullMonthDateYear()
            if (srId != null) {
                binding.tabLayout.visibility = View.GONE
                binding.tabLayout2.visibility = View.GONE
                getVisitReports(srId!!, startDate!!, endDate!!)
            }
        } else {
            binding.tvDateRange.text = getString(R.string.select_date_range)
            binding.tvDateRange.text = customDate
        }

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            isCustomDate = true
            binding.spinnerMenu.setSelection(2)
            binding.dateRangeLayout.isEnabled = true
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            startDate = df.format(sDate)
            endDate = df.format(eDate)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = sDate.formatFullMonthDateYear()
                customDate = sDate.formatFullMonthDateYear()
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    sDate.formatFullMonthDateYear(),
                    eDate.formatFullMonthDateYear()
                )
                customDate =
                    sDate.formatFullMonthDateYear() + " - " + eDate.formatFullMonthDateYear()
            }

            if (srId != null) {
                binding.tabLayout.visibility = View.GONE
                binding.tabLayout2.visibility = View.GONE
                getVisitReports(srId!!, startDate!!, endDate!!)
            }
        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
        }
    }

    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            queue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

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
            soList.clear()
            val obj = JSONObject(response)
            val toArray = obj.getJSONArray("so_list")
            val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
            val soNameList: ArrayList<String> = ArrayList()
            val imageUrl = "null"
            if (soArray.length() > 0) {
                for (i in 0 until soArray.length()) {
                    val soObj = soArray.getJSONObject(i)
                    soList.add(
                        SOList(
                            soObj.getString("id"),
                            soObj.getString("user_name"),
                            soObj.getString("designation"),
                            if (soObj.has("employee_id")) soObj.getString("employee_id") else "",
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
            binding.spinnerSO.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getVisitReports(srId: String, startDate: String, endDate: String) {
        val url = if (sharePrefUtils.getString(Api.USER_TYPE).equals("SO", true)) {
            Api.get_visit_report + "?user_id=" + srId + "&start_date=" + startDate + "&end_date=" + endDate
        } else {
            Api.get_visit_report + "?user_id=" + srId + "&start_date=" + startDate + "&end_date=" + endDate
        }
        binding.progressBar.visibility = View.VISIBLE
        ApiServices.apiGET(
            url,
            queue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    AppLogger.log("getVisitReports:: $response")
                    try {
                        binding.progressBar.visibility = View.GONE
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("visited_report")
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        if (productsArray.length() > 0) {
                            for (i in 0 until productsArray.length()) {
                                val productObj = productsArray.getJSONObject(i)
                                itemList.add(
                                    Pair(
                                        productObj.getString("range"),
                                        productObj.getString("visited_count")
                                            .englishToBanglaNumber()
                                    )
                                )
                            }
                            createTable(itemList, binding.tabLayout)
                        }

                        if (obj.has("total_visited_report") && !obj.isNull("total_visited_report")) {
                            val visitedArray = obj.getJSONArray("total_visited_report")
                            val visitedList: ArrayList<Pair<String, String>> = ArrayList()
                            if (visitedArray.length() > 0) {
                                for (i in 0 until visitedArray.length()) {
                                    val visitedObj = visitedArray.getJSONObject(i)
                                    val keys = visitedObj.keys()
                                    while (keys.hasNext()) {
                                        val key = keys.next() as String
                                        visitedList.add(
                                            Pair(
                                                key,
                                                visitedObj.getString(key)
                                            )
                                        )
                                    }
                                }
                                createTableOther(visitedList, binding.tabLayout2)
                            }
                        }

                    } catch (e: Exception) {
                        binding.progressBar.visibility = View.GONE
                        e.printStackTrace()
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    binding.progressBar.visibility = View.GONE
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        tabLayout.visibility = View.VISIBLE

        for (i in 0 until data.size) {
            val tr = TableRow(applicationContext)
            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.layoutParams = tableRowParams
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(applicationContext)
            c1.gravity = Gravity.START
            c1.setTextColor(ContextCompat.getColor(this@VisitReportActivity, R.color.text_title))
            c1.text = data[i].first
            val c2 = TextView(applicationContext)
            c2.gravity = Gravity.END
            c2.setTextColor(ContextCompat.getColor(this@VisitReportActivity, R.color.text_title))
            c2.text = data[i].second
            c2.gravity = Gravity.CENTER
            c2.background = ContextCompat.getDrawable(
                this@VisitReportActivity,
                R.drawable.button_white_bg_stroke
            )
            tr.addView(c1)
            tr.addView(c2)
            tabLayout.addView(tr)
            tabLayout.background = ContextCompat.getDrawable(
                this@VisitReportActivity,
                R.drawable.cardview_bg_stroke_2dp
            )
        }
    }

    private fun createTableOther(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        tabLayout.visibility = View.VISIBLE

        for (i in 0 until data.size) {
            val tr = TableRow(applicationContext)
            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.layoutParams = tableRowParams
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(applicationContext)
            c1.gravity = Gravity.START
            c1.setTextColor(ContextCompat.getColor(this@VisitReportActivity, R.color.text_title))
            c1.text = data[i].first
            val c2 = TextView(applicationContext)
            c2.gravity = Gravity.END
            c2.setTextColor(ContextCompat.getColor(this@VisitReportActivity, R.color.text_title))
            c2.text = data[i].second
            c2.gravity = Gravity.CENTER
            tr.addView(c1)
            tr.addView(c2)
            tabLayout.addView(tr)
            tabLayout.background = ContextCompat.getDrawable(
                this@VisitReportActivity,
                R.drawable.button_white_bg_stroke
            )
        }
    }
}