package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.databinding.FragmentHomeBinding
import com.barikoi.cnlapp.ui.main.MainActivity.Companion.routeName_selected
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.AppLocale
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.sentry.Sentry
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    val dots: ArrayList<ImageView> = ArrayList()
    var token: String? = ""
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""
    var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        progressBar = view.findViewById(R.id.progressBarHomeTO)

        checkForAttendanceToday()

        AppLogger.log("LOCAL AppLocale:: ${AppLocale.getCurrentLocale(requireContext()).displayName}")
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
                        val attendanceArray = obj.getJSONArray("attendances")
                        if (attendanceArray.length() > 0) {
                            binding.noRouteCheck.visibility = View.GONE
                            binding.bodyLayout.visibility = View.VISIBLE
                            val attendanceObj = attendanceArray.getJSONObject(0)
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
                                if (attendanceObj.getString("route_name").isNotEmpty()) {
                                    routeName_selected!!.visibility = View.VISIBLE
                                    routeName_selected!!.text =
                                        attendanceObj.getString("route_name")
                                } else {
                                    routeName_selected!!.visibility = View.GONE
                                }
                                routeId = attendanceObj.getInt("route_id").toString()
                                if (isAdded) {
                                    init()
                                }
                            } else {
                                progressBar!!.visibility = View.GONE
                                binding.noRouteCheck.visibility = View.VISIBLE
                                binding.bodyLayout.visibility = View.GONE

                                binding.btnTryAgain.setOnClickListener {
                                    checkForAttendanceToday()
                                }
                            }

                        } else {
                            progressBar!!.visibility = View.GONE
                            binding.noRouteCheck.visibility = View.VISIBLE
                            binding.bodyLayout.visibility = View.GONE

                            binding.btnTryAgain.setOnClickListener {
                                checkForAttendanceToday()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                        progressBar!!.visibility = View.GONE
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, mContext!!)
                    progressBar!!.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                    progressBar!!.visibility = View.GONE
                }

            })
    }

    private fun init() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time
        //val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("LLL dd", Locale.getDefault())
        binding.tvDateRange.text =
            resources.getString(
                R.string.date_range_,
                simpleFormat.format(start),
                simpleFormat.format(end)
            )
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(resources.getString(R.string.select_a_date))

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
                binding.tvDateRange.text =
                    simpleFormat.format(s_date) + " - " + simpleFormat.format(
                        e_date
                    )
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getSummaryTargets(
                Api.get_summary + "?start_date=" + df.format(s_date) + " 00:00:00" + "&end_date=" + df.format(
                    e_date
                ) + " 23:59:59" + "&user_id=" + userId/*+"&route_id="+routeId*/
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayoutHome.isEnabled = true
        }

        getSummaryTargets(Api.get_summary + "?start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&user_id=" + userId/*+"&route_id="+routeId*/)
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
                        progressBar!!.visibility = View.GONE
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
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
                        AppLogger.log("itemList:: $itemList")

                        val adapter = TargetAdapter(itemList, "SO")
                        binding.targetListView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        setSecondPartSummary()
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

    private fun setSecondPartSummary() {
        binding.layoutSecond.visibility = View.VISIBLE
        val titles = arrayOf(
            resources.getString(R.string.today_summary),
            resources.getString(R.string.last_week_summary),
            resources.getString(R.string.low_stock_product),
            resources.getString(R.string.last_week_product),
            resources.getString(R.string.today_category),
            resources.getString(R.string.last_week_category),
            resources.getString(R.string.last_week_delivery),
            resources.getString(R.string.bounce_list)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(TodaysSummaryFragment())
        fragments.add(LastWeekSummaryFragment())
        fragments.add(LowStockProductFragment())
        fragments.add(LastWeekProductFragment())
        fragments.add(TodayCategoryFragment())
        fragments.add(LastWeekCategoryFragment())
        fragments.add(LastWeekDeliveryFragment())
        fragments.add(LastWeekBounceFragment())
        binding.dotsLayout.removeAllViews()
        addDots(fragments.size)
        binding.viewPager.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
        TabLayoutMediator(
            binding.viewpagertab, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
        binding.viewPager.currentItem = 0

        for (i in 0 until binding.viewpagertab.tabCount) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectDot(position, fragments.size)
                if (position == 0) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()*/
                } else if (position == 1) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                } else if (position == 2) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                }
            }
        })
    }

    private fun setThirdPartSummary() {
        binding.layoutThird.visibility = View.VISIBLE
        val titles = arrayOf(
            resources.getString(R.string.last_week_delivery),
            resources.getString(R.string.bounce_list)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(LastWeekDeliveryFragment())
        fragments.add(LastWeekBounceFragment())
        binding.viewPagerSecond.setAdapter(
            ViewPagerAdapter(
                parentFragmentManager,
                lifecycle,
                fragments
            )
        )
        // attaching tab mediator
        TabLayoutMediator(
            binding.viewpagertabSecond, binding.viewPagerSecond
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
        binding.viewPagerSecond.currentItem = 0;

        binding.viewPagerSecond.setUserInputEnabled(false)
        for (i in 0 until binding.viewpagertabSecond.tabCount) {
            val tab = (binding.viewpagertabSecond.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + binding.viewPagerSecond.getCurrentItem())
        binding.viewPagerSecond.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
            }
        })
    }

    fun addDots(dotCount: Int) {
        dots.clear()
        for (i in 0 until dotCount) {
            val dot = ImageView(mContext)
            dot.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_dot_unselected
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(2)
            binding.dotsLayout.addView(dot, params)
            dots.add(dot)
        }
    }

    fun selectDot(idx: Int, dotCount: Int) {
        for (i in 0 until dots.size) {
            val drawableId =
                if (i == idx) R.drawable.ic_dot_selected else R.drawable.ic_dot_unselected
            val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
            dots[i].setImageDrawable(drawable)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }
}