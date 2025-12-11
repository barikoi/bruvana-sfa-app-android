package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Activity.ActiveInactiveActivity
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.databinding.FragmentHomeTOBinding
import com.barikoi.cnlapp.ui.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.ui.adapter.ViewPagerAdapter
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.englishToBanglaNumber
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class  HomeTOFragment : Fragment() {
    private lateinit var binding: FragmentHomeTOBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils


    @Inject
    lateinit var mQueue: RequestQueue


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeTOBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            mQueue, sharePrefUtils.getString(Api.TOKEN)!!, object : ApiServiceListener {
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
                            binding.bodyLayout.visibility = View.GONE

                            binding.btnTryAgain.setOnClickListener {
                                checkForAttendanceToday()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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
        val startDate = df.format(start)
        val endDate = df.format(end)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayoutHome.setOnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayoutHome.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayoutHome.isEnabled = true
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = simpleFormat.format(sDate)
                sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, df.format(sDate))
                sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, df.format(sDate))
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(sDate),
                    simpleFormat.format(eDate)
                )
                sharePrefUtils.saveString(Api.START_DATE_ATTENDANCE, df.format(sDate))
                sharePrefUtils.saveString(Api.END_DATE_ATTENDANCE, df.format(eDate))
            }


            if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
                getSummaryTargets(
                    Api.get_summary + "?start_date=" + df.format(sDate) + " 00:00:00" + "&end_date=" + df.format(
                        eDate
                    ) + " 23:59:59" + "&with_to_stats=1&territory_id=" + sharePrefUtils.getString(
                        Api.TERRITORY_ID
                    ) + "&user_id=" + sharePrefUtils.getString(
                        Api.USER_ID
                    )
                )
            } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)) {
                getSummaryTargets(
                    Api.get_summary + "?start_date=" + df.format(sDate) + " 00:00:00" + "&end_date=" + df.format(
                        eDate
                    ) + " 23:59:59" + "&with_asm_stats=1&region_id=" + sharePrefUtils.getString(
                        Constants.REGION_ID
                    ) + "&user_id=" + sharePrefUtils.getString(Api.USER_ID)
                )
            }
        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayoutHome.isEnabled =
                true
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            getSummaryTargets(
                Api.get_summary + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&with_to_stats=1&territory_id=" + sharePrefUtils.getString(
                    Api.TERRITORY_ID
                ) + "&user_id=" + sharePrefUtils.getString(Api.USER_ID)
            )

        } else if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)) {
            getSummaryTargets(
                Api.get_summary + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&with_asm_stats=1&region_id=" + sharePrefUtils.getString(
                    Constants.REGION_ID
                ) + "&user_id=" + sharePrefUtils.getString(Api.USER_ID)
            )
        }
    }

    private fun getSummaryTargets(url: String) {
        var totalTarget = "--:--"
        var totalTargetValue = 0.0
        var totalTargetCompleted = "--:--"
        var totalTargetCompletedValue = 0.0

        var lpc = "--:--"
        var lpcValue = 0.0
        var lpcCompleted = "--:--"
        var lpcCompletedValue = 0.0

        var bpc = "--:--"
        var bpcValue = 0.0
        var bpcCompleted = "--:--"
        var bpcCompletedValue = 0.0

        var aiv = "--:--"
        var aivValue = 0.0
        var aivCompleted = "--:--"
        var aivCompletedValue = 0.0

        var ads = "--:--"
        var adsValue = 0.0
        var adsCompleted = "--:--"
        var adsCompletedValue = 0.0

        var rds = "--:--"
        var rdsValue = 0.0
        var rdsCompleted = "--:--"
        var rdsCompletedValue = 0.0

        var visited = "--:--"
        var visitedValue = 0.0
        var visitCompleted = "--:--"
        var visitCompletedValue = 0.0

        var bounced = "--:--"
        var bouncedValue = 0.0
        var bounceCompleted = "--:--"
        var bounceCompletedValue = 0.0

        val dFormat = DecimalFormat("#.##")
        ApiServices.apiGET(
            url,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponseSuccess(response: String) {
                    try {
                        AppLogger.log("getSummaryTargets:: $response")
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        binding.noRouteCheck.visibility = View.GONE
                        binding.bodyLayout.visibility = View.VISIBLE
                        if (targetsArray.length() > 0) {
                            for (i in 0 until targetsArray.length()) {
                                val targetObj = targetsArray.getJSONObject(i)
                                if (!targetObj.isNull("target_amount")) {
                                    totalTarget = dFormat.format(
                                        targetObj.getString("target_amount").toDouble()
                                    )
                                    totalTargetValue =
                                        targetObj.getString("target_amount").toDouble()
                                }
                                if (!targetObj.isNull("target_ads")) {
                                    ads =
                                        dFormat.format(targetObj.getString("target_ads").toDouble())
                                    adsValue = targetObj.getString("target_ads").toDouble()
                                }
                                if (!targetObj.isNull("target_rds")) {
                                    rds =
                                        dFormat.format(targetObj.getString("target_rds").toDouble())
                                    rdsValue = targetObj.getString("target_rds").toDouble()
                                }
                                if (!targetObj.isNull("target_sku_per_memo")) {
                                    bpc = dFormat.format(
                                        targetObj.getString("target_sku_per_memo").toDouble()
                                    )
                                    bpcValue =
                                        targetObj.getString("target_sku_per_memo").toDouble()
                                }
                                if (!targetObj.isNull("target_number_of_memo")) {
                                    lpc =
                                        dFormat.format(
                                            targetObj.getString("target_number_of_memo").toDouble()
                                        )
                                    lpcValue =
                                        targetObj.getString("target_number_of_memo").toDouble()
                                }
                                if (!targetObj.isNull("target_number_of_visits")) {
                                    visited =
                                        dFormat.format(
                                            targetObj.getString("target_number_of_visits")
                                                .toDouble()
                                        )
                                    visitedValue =
                                        targetObj.getString("target_number_of_visits").toDouble()
                                }
                                if (!targetObj.isNull("target_aiv")) {
                                    aiv =
                                        dFormat.format(targetObj.getString("target_aiv").toDouble())
                                    aivValue = targetObj.getString("target_aiv").toDouble()
                                }
                                if (!targetObj.isNull("threshold_bounce_percentage")) {
                                    bounced =
                                        dFormat.format(
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
                                    totalTargetCompleted =
                                        dFormat.format(targetObj.getString("revenue").toDouble())
                                    totalTargetCompletedValue =
                                        targetObj.getString("revenue").toDouble()
                                }
                                if (!targetObj.isNull("ads")) {
                                    adsCompleted =
                                        dFormat.format(targetObj.getString("ads").toDouble())
                                    adsCompletedValue = targetObj.getString("ads").toDouble()
                                }
                                if (!targetObj.isNull("rds")) {
                                    rdsCompleted =
                                        dFormat.format(targetObj.getString("rds").toDouble())
                                    rdsCompletedValue = targetObj.getString("rds").toDouble()
                                }
                                if (!targetObj.isNull("sku_per_memo")) {
                                    bpcCompleted =
                                        dFormat.format(
                                            targetObj.getString("sku_per_memo").toDouble()
                                        )
                                    bpcCompletedValue =
                                        targetObj.getString("sku_per_memo").toDouble()
                                }
                                if (!targetObj.isNull("number_of_memo")) lpcCompleted =
                                    dFormat.format(targetObj.getString("number_of_memo").toDouble())
                                lpcCompletedValue = targetObj.getString("number_of_memo").toDouble()
                                if (!targetObj.isNull("number_of_visits")) {
                                    visitCompleted =
                                        dFormat.format(
                                            targetObj.getString("number_of_visits").toDouble()
                                        )
                                    visitCompletedValue =
                                        targetObj.getString("number_of_visits").toDouble()
                                }
                                if (!targetObj.isNull("aiv")) {
                                    aivCompleted =
                                        dFormat.format(targetObj.getString("aiv").toDouble())
                                    aivCompletedValue = targetObj.getString("aiv").toDouble()
                                }
                                if (!targetObj.isNull("bounce_amount_percentage")) {
                                    bounceCompleted =
                                        dFormat.format(
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
                                totalTarget,
                                totalTargetValue,
                                totalTargetCompleted,
                                totalTargetCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.ads),
                                ads,
                                adsValue,
                                adsCompleted,
                                adsCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.rds),
                                rds,
                                rdsValue,
                                rdsCompleted,
                                rdsCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.sku_per_memo),
                                bpc,
                                bpcValue,
                                bpcCompleted,
                                bpcCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.number_of_memo),
                                lpc,
                                lpcValue,
                                lpcCompleted,
                                lpcCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.visit_ratio),
                                visited,
                                visitedValue,
                                visitCompleted,
                                visitCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.aiv),
                                aiv,
                                aivValue,
                                aivCompleted,
                                aivCompletedValue
                            )
                        )
                        itemList.add(
                            TargetValue(
                                resources.getString(R.string.bounce) + " (%)",
                                bounced,
                                bouncedValue,
                                bounceCompleted,
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

                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.progressBarHomeTO.visibility = View.GONE
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                    binding.progressBarHomeTO.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    binding.progressBarHomeTO.visibility = View.GONE
                }

            })

    }

    private fun setActiveInactiveView() {
        binding.layoutSecond.visibility = View.VISIBLE
        val gd = GradientDrawable()
        gd.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        gd.cornerRadius = 16f
        gd.setStroke(3, ContextCompat.getColor(requireContext(), R.color.cnl_color_2))
        binding.activeLayout.background = gd
        val gd2 = GradientDrawable()
        gd2.setColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        gd2.cornerRadius = 16f
        gd2.setStroke(3, ContextCompat.getColor(requireContext(), R.color.cnl_color_1))
        binding.inactiveLayout.background = gd2

        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(
            Api.get_attendance + "?start_date=" + df.format(today) + "&end_date=" + df.format(today) + "&with_active_inactive_so=1",
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                @SuppressLint("SetTextI18n")
                override fun onResponseSuccess(response: String) {
                    try {
                        val obj = JSONObject(response)
                        val activeSO = obj.getJSONArray("active").length()
                        val inactiveSO = obj.getJSONArray("inactive").length()
                        binding.activeCount.text = activeSO.toString().englishToBanglaNumber()
                        binding.inactiveCount.text =
                            inactiveSO.toString().englishToBanglaNumber()
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                    binding.progressBarHomeTO.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    binding.progressBarHomeTO.visibility = View.GONE
                }

            })
    }

    private fun setLiveStockView() {
        val itemList: ArrayList<Pair<String, String>> = ArrayList()
        val today = Calendar.getInstance().time

        ApiServices.apiGET(
            Api.all_product_list + "?start_date=" + today + " 00:00:00" + "&end_date=" + today + " 23:59:59" + "&with_stock=1&territory_id=" + sharePrefUtils.getString(
                Api.TERRITORY_ID
            ),
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
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

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {}

            })
    }

    private fun setSummary() {
        binding.layoutFourth.visibility = View.VISIBLE
        binding.progressBarHomeTO.visibility = View.GONE
        val titles = arrayOf(
            resources.getString(R.string.today_summary),
            resources.getString(R.string.last_week_summary)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(TodaySummaryTOFragment())
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
        for (i in 0 until binding.viewpagertab.tabCount) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + binding.viewPager.currentItem)
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        val size: Int = if (data.size < 5) {
            data.size
        } else {
            5
        }
        for (i in 0 until size) {
            val tr = TableRow(requireContext())
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
            val c1 = TextView(requireContext())
            c1.gravity = Gravity.START
            c1.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_title
                )
            )
            c1.text = data[i].first
            val c2 = TextView(requireContext())
            c2.gravity = Gravity.END
            c2.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.text_title
                )
            )
            c2.text = data[i].second
            c2.gravity = Gravity.CENTER
            c2.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_white_bg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tabLayout.addView(tr)
        }
    }

}