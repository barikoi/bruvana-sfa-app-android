@file:Suppress("DEPRECATION")

package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentLastWeekSummaryTOBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class LastWeekSummaryTOFragment : Fragment() {
    private lateinit var binding: FragmentLastWeekSummaryTOBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var mQueue: RequestQueue


    var pd: ProgressDialog? = null
    var startDate: String? = null
    var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLastWeekSummaryTOBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
//            setTodaySummaryForASM()
        } else {
            setLastWeekSummary()
        }


        binding.tryAgain.setOnClickListener {
            if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
//            setTodaySummaryForASM()
            } else {
                setLastWeekSummary()
            }

        }
    }

    private fun setLastWeekSummary() {
        try {
            binding.progressBarHome.visibility = View.VISIBLE
            binding.summaryLayout.visibility = View.GONE
            val dformat = DecimalFormat("#.##")
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_WEEK, -7)
            val start = c.time
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            startDate = df.format(start)
            endDate = df.format(start)
            ApiServices.apiGET(
                Api.get_all_so_list + "?last_week_summary=1&start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&to_id=" + sharePrefUtils.getString(
                    Api.USER_ID
                ),
                mQueue,
                sharePrefUtils.getString(Api.TOKEN)!!,
                object :
                    ApiServiceListener {
                    override fun onResponseSuccess(response: String) {
                        try {
                            binding.progressBarHome.visibility = View.GONE
                            binding.summaryLayout.visibility = View.VISIBLE
                            binding.tryAgain.visibility = View.GONE
                            val obj = JSONObject(response)
                            val toArray = obj.getJSONArray("so_list")
                            val toObj = toArray.getJSONObject(0)
                            val ordersArray = toObj.getJSONArray("sales_officers")
                            val itemList: ArrayList<Pair<Pair<String, String>, String>> =
                                ArrayList()
                            if (ordersArray.length() > 0) {
                                for (i in 0 until ordersArray.length()) {
                                    val orderObj = ordersArray.getJSONObject(i)
                                    itemList.add(
                                        Pair(
                                            Pair(
                                                orderObj.getString("user_name"),
                                                orderObj.getString("id")
                                            ),
                                            dformat.format(
                                                orderObj.getString("so_ordered_value")
                                                    .toDouble()
                                            )
                                        )
                                    )
                                }
                            }
                            if (!toObj.getString("order_amount")
                                    .equals("null")
                            ) binding.ovCount.text = dformat.format(
                                toObj.getString("order_amount").toDouble()
                            )
                            if (!toObj.getString("sku_per_memo")
                                    .equals("null")
                            ) binding.bpcCount.text = dformat.format(
                                toObj.getString("sku_per_memo").toDouble()
                            )
                            if (!toObj.getString("number_of_memo")
                                    .equals("null")
                            ) binding.lpcCount.text = dformat.format(
                                toObj.getString("number_of_memo").toDouble()
                            )
                            createTableClickable(itemList, binding.tabLayout2)

                        } catch (e: Exception) {
                            e.printStackTrace()
                            binding.progressBarHome.visibility = View.GONE
                            binding.summaryLayout.visibility = View.GONE
                            binding.tryAgain.visibility = View.VISIBLE
                        }

                    }

                    override fun onJSONResponseSuccess(response: JSONObject) {}

                    override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                    override fun onResponseFailure(error: VolleyError) {
                        try {
                            ViewUtils.getErrorResponse(error, requireContext())
                            binding.progressBarHome.visibility = View.GONE
                            binding.summaryLayout.visibility = View.GONE
                            binding.tryAgain.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onException(e: Exception) {
                        try {
                            binding.progressBarHome.visibility = View.GONE
                            binding.summaryLayout.visibility = View.GONE
                            binding.tryAgain.visibility = View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        if (data.size > 0) {
            for (i in 0 until data.size) {
                val tr = TableRow(requireContext())
                val c1 = TextView(requireContext())
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.text = data[i].first
                val c2 = TextView(requireContext())
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.text = data[i].second
                tr.addView(c1)
                tr.addView(c2)
                tabLayout.addView(tr)
            }
        }
        pd!!.dismiss()
    }

    private fun createTableClickable(
        data: ArrayList<Pair<Pair<String, String>, String>>,
        tabLayout: TableLayout
    ) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        binding.tabLayoutTarget.removeAllViews()
        binding.targetLayout.visibility = View.GONE
        if (data.size > 0) {
            for (i in 0 until data.size) {
                val tr = TableRow(requireContext())
                val tableRowParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                val leftMargin = 0
                val topMargin = 0
                val rightMargin = 0
                val bottomMargin = 8

                tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
                tr.layoutParams = tableRowParams
                tr.gravity = Gravity.CENTER_VERTICAL
                tr.tag = i
                val c1 = TextView(requireContext())
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.text = data[i].first.first
                val c2 = TextView(requireContext())
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.text = data[i].second
                c2.gravity = Gravity.CENTER
                c2.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.button_white_bg_stroke)
                tr.addView(c1)
                tr.addView(c2)

                tr.setOnClickListener {
                    Log.d("OrderSummary", "pd.isShowing: " + pd!!.isShowing)
                    pd!!.show()

                    try {
                        getSummaryTargets(Api.get_summary + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&user_id=" + data[i].first.second)
                        Log.d("OrderSummary", "row count: " + tabLayout.childCount)
                        for (t in 0 until tabLayout.childCount) {
                            if (tabLayout.getChildAt(t).tag == it.tag) {
                                tabLayout.getChildAt(t)
                                    .setBackgroundColor(resources.getColor(R.color.light_yellow))
                            } else {
                                tabLayout.getChildAt(t)
                                    .setBackgroundColor(resources.getColor(R.color.white))
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }


                }
                tabLayout.addView(tr)
            }
        }
        pd!!.dismiss()
    }

    private fun getSummaryTargets(url: String) {
        var totalTargetCompleted = "--:--"
        var lpcCompleted = "--:--"
        var bpcCompleted = "--:--"
        var aivCompleted = "--:--"
        var adsCompleted = "--:--"
        var rdsCompleted = "--:--"
        var visitCompleted = "--:--"
        var bounceCompleted = "--:--"
        var deliveryValue = "--:--"
        val dFormat = DecimalFormat("#.##")
        ApiServices.apiGET(
            url,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        binding.progressBarHome.visibility = View.GONE
                        binding.targetLayout.visibility = View.VISIBLE
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("revenue")) totalTargetCompleted =
                                    dFormat.format(targetObj.getString("revenue").toDouble())
                                if (!targetObj.isNull("ads")) adsCompleted =
                                    dFormat.format(targetObj.getString("ads").toDouble())
                                if (!targetObj.isNull("rds")) rdsCompleted =
                                    dFormat.format(targetObj.getString("rds").toDouble())
                                if (!targetObj.isNull("sku_per_memo")) bpcCompleted =
                                    dFormat.format(targetObj.getString("sku_per_memo").toDouble())
                                if (!targetObj.isNull("number_of_memo")) lpcCompleted =
                                    dFormat.format(targetObj.getString("number_of_memo").toDouble())
                                if (!targetObj.isNull("number_of_visits")) visitCompleted =
                                    dFormat.format(
                                        targetObj.getString("number_of_visits").toDouble()
                                    )
                                if (!targetObj.isNull("aiv")) aivCompleted =
                                    dFormat.format(targetObj.getString("aiv").toDouble())
                                if (!targetObj.isNull("bounce_amount_percentage")) bounceCompleted =
                                    dFormat.format(
                                        targetObj.getString("bounce_amount_percentage").toDouble()
                                    )
                                if (!targetObj.isNull("delivered_value")) deliveryValue =
                                    dFormat.format(
                                        targetObj.getString("delivered_value").toDouble()
                                    )
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(
                            Pair(
                                resources.getString(R.string.total_delivery_value),
                                totalTargetCompleted
                            )
                        )
                        itemList.add(Pair(resources.getString(R.string.ads), adsCompleted))
                        itemList.add(Pair(resources.getString(R.string.rds), rdsCompleted))
                        itemList.add(
                            Pair(
                                resources.getString(R.string.sku_per_memo),
                                bpcCompleted
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.number_of_memo),
                                lpcCompleted
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.visit_ratio),
                                visitCompleted
                            )
                        )
                        itemList.add(Pair(resources.getString(R.string.aiv), aivCompleted))
                        itemList.add(
                            Pair(
                                resources.getString(R.string.bounce) + " (%)",
                                bounceCompleted
                            )
                        )
                        createTable(itemList, binding.tabLayoutTarget)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.progressBarHome.visibility = View.GONE
                        //getAllOrders(orderArray!!)
                        pd!!.dismiss()
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                    binding.progressBarHome.visibility = View.GONE
                    pd!!.dismiss()
                }

                override fun onException(e: Exception) {
                    binding.progressBarHome.visibility = View.GONE
                    pd!!.dismiss()
                }

            })

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        pd = ProgressDialog(requireContext())
        pd!!.setMessage("Processing...")
        pd!!.setCancelable(false)
    }
}