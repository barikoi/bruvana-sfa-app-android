package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Activity.ActiveInactiveActivity
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.databinding.FragmentHomeTOBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeTOFragment : Fragment() {
    private lateinit var binding: FragmentHomeTOBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = ""
    private var territoryId: String? = ""
    private var employeeId: String? = ""
    var userId: String? = ""
    var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeTOBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressBarHomeTO)

        init()

        binding.liveStockUpdate.setOnClickListener {
            startActivity(Intent(requireActivity(), ProductStockUpdateActivity::class.java))
        }

        binding.lastweeksummary.setOnClickListener {
            startActivity(
                Intent(
                    requireActivity(),
                    OrderSummaryTOActivity::class.java
                ).putExtra("from", "lastweek")
            )
        }


    }

    private fun checkForAttendanceToday() {
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(
            Api.get_attendance + "?start_date=" + df.format(today) + "&end_date=" + df.format(today),
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val obj = JSONObject(response)
                        val attedanceArray = obj.getJSONArray("attendances")
                        if (attedanceArray.length() > 0) {
                            binding.noRouteCheck.visibility = View.GONE
                            binding.bodyLayout.visibility = View.VISIBLE
                            init()
                        } else {
                            binding.noRouteCheck.visibility = View.VISIBLE
                            binding. bodyLayout.visibility = View.GONE

                            binding.btnTryAgain.setOnClickListener {
                                checkForAttendanceToday()
                            }
                        }
                    } catch (e: Exception) {
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
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("LLL dd", Locale.getDefault())
        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayoutHome.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayoutHome.setEnabled(false)
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayoutHome.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                binding.tvDateRange.text = simpleFormat.format(s_date)
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()
            } else {
                binding.tvDateRange.text = simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date)
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getSummaryTargets(
                Api.get_summary + "?start_date=" + df.format(s_date) + " 00:00:00" + "&end_date=" + df.format(
                    e_date
                ) + " 23:59:59" + "&with_to_stats=1&territory_id=" + territoryId + "&user_id=" + userId
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener { binding.dateRangeLayoutHome.setEnabled(true) }

        getSummaryTargets(Api.get_summary + "?start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&with_to_stats=1&territory_id=" + territoryId + "&user_id=" + userId)

    }

    private fun getSummaryTargets(url: String) {
        var total_target = "--:--"
        var totalTargetValue = 0.0
        var total_target_completed = "--:--"
        var totalTargetCompletedValue = 0.0

        var lpc = "--:--"
        var lpcValue = 0.0
        var lpc_completed = "--:--"
        var lpcCompletedValue = 0.0

        var bpc = "--:--"
        var bpcValue = 0.0
        var bpc_completed = "--:--"
        var bpcCompletedValue = 0.0

        var aiv = "--:--"
        var aivValue = 0.0
        var aiv_completed = "--:--"
        var aivCompletedValue = 0.0

        var ads = "--:--"
        var adsValue = 0.0
        var ads_completed = "--:--"
        var adsCompletedValue = 0.0

        var rds = "--:--"
        var rdsValue = 0.0
        var rds_completed = "--:--"
        var rdsCompletedValue = 0.0

        var visited = "--:--"
        var visitedValue = 0.0
        var visit_completed = "--:--"
        var visitCompletedValue = 0.0

        var bounced = "--:--"
        var bouncedValue = 0.0
        var bounce_completed = "--:--"
        var bounceCompletedValue = 0.0

        val dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    AppLogger.log("getSummaryTargets:: $response")
                    if (response != null) {
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        binding.noRouteCheck.visibility = View.GONE
                        binding.bodyLayout.visibility = View.VISIBLE
                        if (targetsArray.length() > 0) {
                            for (i in 0 until targetsArray.length()) {
                                val targetObj = targetsArray.getJSONObject(i)
                                if (!targetObj.isNull("target_amount")) {
                                    total_target = dformat.format(
                                        targetObj.getString("target_amount").toDouble()
                                    )
                                    totalTargetValue =
                                        targetObj.getString("target_amount").toDouble()
                                }
                                if (!targetObj.isNull("target_ads")) {
                                    ads =
                                        dformat.format(targetObj.getString("target_ads").toDouble())
                                    adsValue = targetObj.getString("target_ads").toDouble()
                                }
                                if (!targetObj.isNull("target_rds")) {
                                    rds =
                                        dformat.format(targetObj.getString("target_rds").toDouble())
                                    rdsValue = targetObj.getString("target_rds").toDouble()
                                }
                                if (!targetObj.isNull("target_sku_per_memo")) {
                                    bpc = dformat.format(
                                        targetObj.getString("target_sku_per_memo").toDouble()
                                    )
                                    bpcValue =
                                        targetObj.getString("target_sku_per_memo").toDouble()
                                }
                                if (!targetObj.isNull("target_number_of_memo")) {
                                    lpc =
                                        dformat.format(
                                            targetObj.getString("target_number_of_memo").toDouble()
                                        )
                                    lpcValue =
                                        targetObj.getString("target_number_of_memo").toDouble()
                                }
                                if (!targetObj.isNull("target_number_of_visits")) {
                                    visited =
                                        dformat.format(
                                            targetObj.getString("target_number_of_visits")
                                                .toDouble()
                                        )
                                    visitedValue =
                                        targetObj.getString("target_number_of_visits").toDouble()
                                }
                                if (!targetObj.isNull("target_aiv")) {
                                    aiv =
                                        dformat.format(targetObj.getString("target_aiv").toDouble())
                                    aivValue = targetObj.getString("target_aiv").toDouble()
                                }
                                if (!targetObj.isNull("threshold_bounce_percentage")) {
                                    bounced =
                                        dformat.format(
                                            targetObj.getString("threshold_bounce_percentage")
                                                .toDouble()
                                        )

                                    bouncedValue =
                                        targetObj.getString("threshold_bounce_percentage")
                                            .toDouble()
                                }
                            }
                        }
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("revenue")) {
                                    total_target_completed =
                                        dformat.format(targetObj.getString("revenue").toDouble())
                                    totalTargetCompletedValue =
                                        targetObj.getString("revenue").toDouble()
                                }
                                if (!targetObj.isNull("ads")) {
                                    ads_completed =
                                        dformat.format(targetObj.getString("ads").toDouble())
                                    adsCompletedValue = targetObj.getString("ads").toDouble()
                                }
                                if (!targetObj.isNull("rds")) {
                                    rds_completed =
                                        dformat.format(targetObj.getString("rds").toDouble())
                                    rdsCompletedValue = targetObj.getString("rds").toDouble()
                                }
                                if (!targetObj.isNull("sku_per_memo")) {
                                    bpc_completed =
                                        dformat.format(
                                            targetObj.getString("sku_per_memo").toDouble()
                                        )
                                    bpcCompletedValue =
                                        targetObj.getString("sku_per_memo").toDouble()
                                }
                                if (!targetObj.isNull("number_of_memo")) lpc_completed =
                                    dformat.format(targetObj.getString("number_of_memo").toDouble())
                                lpcCompletedValue = targetObj.getString("number_of_memo").toDouble()
                                if (!targetObj.isNull("number_of_visits")) {
                                    visit_completed =
                                        dformat.format(
                                            targetObj.getString("number_of_visits").toDouble()
                                        )
                                    visitCompletedValue =
                                        targetObj.getString("number_of_visits").toDouble()
                                }
                                if (!targetObj.isNull("aiv")) {
                                    aiv_completed =
                                        dformat.format(targetObj.getString("aiv").toDouble())
                                    aivCompletedValue = targetObj.getString("aiv").toDouble()
                                }
                                if (!targetObj.isNull("bounce_amount_percentage")) {
                                    bounce_completed =
                                        dformat.format(
                                            targetObj.getString("bounce_amount_percentage")
                                                .toDouble()
                                        )
                                    bounceCompletedValue =
                                        targetObj.getString("bounce_amount_percentage").toDouble()
                                }
                            }
                        }

                        val itemList: ArrayList<TargetValue> = ArrayList()
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.total_target),
                                total_target,
                                totalTargetValue,
                                total_target_completed,
                                totalTargetCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.ads),
                                ads,
                                adsValue,
                                ads_completed,
                                adsCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.rds),
                                rds,
                                rdsValue,
                                rds_completed,
                                rdsCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.sku_per_memo),
                                bpc,
                                bpcValue,
                                bpc_completed,
                                bpcCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.number_of_memo),
                                lpc,
                                lpcValue,
                                lpc_completed,
                                lpcCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.visit_ratio),
                                visited,
                                visitedValue,
                                visit_completed,
                                visitCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.aiv),
                                aiv,
                                aivValue,
                                aiv_completed,
                                aivCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.bounce) + " (%)",
                                bounced,
                                bouncedValue,
                                bounce_completed,
                                bounceCompletedValue
                            )
                        )

                        AppLogger.log("Target:: $itemList")

                        val adapter = TargetAdapter(itemList, "TO")
                        binding.targetListView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        setActiveInactiveView()
                        //setLiveStockView()
                        setSummary()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBar!!.visibility = View.GONE
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
                progressBar!!.visibility = View.GONE
            }

            override fun onException(e: Exception) {
                progressBar!!.visibility = View.GONE
            }

        })

    }

    private fun setActiveInactiveView() {
        binding.layoutSecond.visibility = View.VISIBLE
        val gd = GradientDrawable()
        gd.setColor(mContext!!.resources.getColor(R.color.white))
        gd.cornerRadius = 16f
        gd.setStroke(3, mContext!!.resources.getColor(R.color.cnl_color_2))
        binding.activeLayout.setBackgroundDrawable(gd)
        val gd2 = GradientDrawable()
        gd2.setColor(mContext!!.resources.getColor(R.color.white))
        gd2.cornerRadius = 16f
        gd2.setStroke(3, mContext!!.resources.getColor(R.color.cnl_color_1))
        binding.inactiveLayout.setBackgroundDrawable(gd2)

        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(
            Api.get_attendance + "?start_date=" + df.format(today) + "&end_date=" + df.format(today) + "&with_active_inactive_so=1",
            mQueue!!,
            token!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null) {
                            val obj = JSONObject(response)
                            //val attendanceArray = obj.getJSONArray("active")
                            val activeSO = obj.getJSONArray("active").length()
                            val inactiveSO = obj.getJSONArray("inactive").length()
                            binding.activeCount.text = activeSO.toString().englishToBanglaNumber()
                            binding.inactiveCount.text = inactiveSO.toString().englishToBanglaNumber()
                            if (obj.getJSONArray("active").length() > 0) {
                                binding.activeLayout.setOnClickListener {
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            ActiveInactiveActivity::class.java
                                        ).putExtra("so_status", "active")
                                    )
                                }
                            }
                            if (obj.getJSONArray("inactive").length() > 0) {
                                binding.inactiveLayout.setOnClickListener {
                                    startActivity(
                                        Intent(
                                            requireActivity(),
                                            ActiveInactiveActivity::class.java
                                        ).putExtra("so_status", "inactive")
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
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
                    progressBar!!.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    progressBar!!.visibility = View.GONE
                }

            })
    }

    private fun setLiveStockView() {
        val itemList: ArrayList<Pair<String, String>> = ArrayList()
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(
            Api.all_product_list + "?start_date=" + today + " 00:00:00" + "&end_date=" + today + " 23:59:59" + "&with_stock=1&territory_id=" + territoryId,
            mQueue!!,
            token!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null) {
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
                                createTable(itemList, binding.tabLayout)
                            }

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, mContext!!)
                }

                override fun onException(e: Exception) {}

            })
    }

    private fun setSummary() {
        binding. layoutFourth.visibility = View.VISIBLE
        progressBar!!.visibility = View.GONE
        val titles = arrayOf(
            resources.getString(R.string.today_summary),
            resources.getString(R.string.last_week_summary)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(TodaysSummaryTOFragment())
        fragments.add(LastWeekSummaryTOFragment())
        binding.viewPager.adapter = ViewPagerAdapter(parentFragmentManager, lifecycle, fragments)
        // attaching tab mediator
        TabLayoutMediator(
            binding.viewpagertab, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
        binding.viewPager.currentItem = 0

        //viewPager.setUserInputEnabled(false)
        for (i in 0 until binding.viewpagertab.getTabCount()) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + binding.viewPager.getCurrentItem())
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        var size: Int = 0
        if (data.size < 5) {
            size = data.size
        } else {
            size = 5
        }
        for (i in 0 until size) {
            val tr = TableRow(mContext)
            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
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
            c2.background = resources.getDrawable(R.drawable.button_white_bg_stroke)
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
        userId = prefs!!.getString(Api.USER_ID, "")
    }


}