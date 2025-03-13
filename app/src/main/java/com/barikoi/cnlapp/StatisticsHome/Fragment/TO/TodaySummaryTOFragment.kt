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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.FragmentTodaysSummaryTOBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.toast
import com.barikoi.cnlapp.utils.extension.totalAmountFormatted
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class TodaySummaryTOFragment : Fragment() {
    private lateinit var binding: FragmentTodaysSummaryTOBinding

    private val viewModel: TodaySummeryTOViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils


    val dFormat = DecimalFormat("#.##")


    private val itemListDetails: MutableList<ArrayList<Pair<String, String>>> = mutableListOf()


    @Inject
    lateinit var mQueue: RequestQueue

    var pd: ProgressDialog? = null

    var endDate: String? = null
    private var soId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodaysSummaryTOBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        endDate = Calendar.getInstance().time.formatDate()

        startTodaySummaryObserve()

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            viewModel.getTodaySummary(
                endDate!!,
                endDate!!,
                "1"
            )
        } else {
            setTodaySummary()
        }

        binding.tryAgain.setOnClickListener {
            if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                viewModel.getTodaySummary(
                    endDate!!,
                    endDate!!,
                    "1"
                )
            } else {
                setTodaySummary()
            }
        }
    }

    private fun setTodaySummary() {
        try {
            binding.progressBarHome.visibility = View.VISIBLE
            binding.summaryLayout.visibility = View.GONE
            val dFormat = DecimalFormat("#.##")

            val end = Calendar.getInstance().time
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            endDate = df.format(end)

            ApiServices.apiGET(
                Api.get_all_so_list + "?today_summary=1&start_date=" + endDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&to_id=" + sharePrefUtils.getString(
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
                                            dFormat.format(
                                                orderObj.getString("so_ordered_value")
                                                    .toDouble()
                                            )
                                        )
                                    )
                                }
                            }
                            if (!toObj.getString("order_amount")
                                    .equals("null")
                            ) binding.ovCount.text = dFormat.format(
                                toObj.getString("order_amount").toDouble()
                            )
                            if (!toObj.getString("sku_per_memo")
                                    .equals("null")
                            ) binding.bpcCount.text = dFormat.format(
                                toObj.getString("sku_per_memo").toDouble()
                            )
                            if (!toObj.getString("number_of_memo")
                                    .equals("null")
                            ) binding.lpcCount.text = dFormat.format(
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

    private fun startTodaySummaryObserve() {
        lifecycleScope.launch {
            viewModel.todaySummaryResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startTodaySummaryObserve::Empty")

                        binding.progressBarHome.visibility = View.GONE
                        binding.summaryLayout.visibility = View.GONE
                        binding.tryAgain.visibility = View.GONE
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startTodaySummaryObserve::Error ${it.error}")

                        binding.progressBarHome.visibility = View.GONE
                        binding.summaryLayout.visibility = View.GONE
                        binding.tryAgain.visibility = View.VISIBLE

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startTodaySummaryObserve::Loading")

                        binding.progressBarHome.visibility = View.VISIBLE
                        binding.summaryLayout.visibility = View.GONE
                        binding.tryAgain.visibility = View.GONE
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startTodaySummaryObserve:: Success ${it.data}")

                        binding.progressBarHome.visibility = View.GONE
                        binding.summaryLayout.visibility = View.VISIBLE
                        binding.tryAgain.visibility = View.GONE


                        val itemList: ArrayList<Pair<Pair<String, String>, String>> = ArrayList()

                        val nm = it.data?.toList?.sumOf { s ->
                            s.totalOrders
                        }

                        val ov = it.data?.toList?.sumOf { s ->
                            s.totalOrderedAmount.toDoubleOrNull()
                                ?: 0.0  // Convert to Double, default to 0.0 if conversion fails
                        }

                        val totalSku = it.data?.toList?.sumOf { s ->
                            s.numOfSku
                        }

                        val spm = totalSku?.takeIf { nm != null && nm != 0 }?.div(nm!!) ?: 0

                        binding.ovCount.text = dFormat.format(ov)
                        binding.bpcCount.text = dFormat.format(spm)
                        binding.lpcCount.text = nm.toString()


                        it.data?.toList!!.forEach { to ->
                            val keyPair = Pair(to.toName, to.toId.toString())
                            val value = to.totalAmountFormatted()
                            itemList.add(Pair(keyPair, value))

                            val skuPerMemo =
                                to.numOfSku.takeIf { to.totalOrders != 0 }?.div(to.totalOrders) ?: 0

                            val itemListDetailsTmp = ArrayList<Pair<String, String>>()
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.total_order_value),
                                    to.totalAmountFormatted()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.sku_per_memo),
                                    skuPerMemo.toString()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.number_of_memo),
                                    to.totalOrders.toString()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.visit_ratio),
                                    to.numOfVisits.toString()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.visit_500m),
                                    to.oneToHundred.toString()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.aiv),
                                    to.aiv.toString().totalAmountFormatted()
                                )
                            )


                            itemListDetails.add(itemListDetailsTmp)
                        }

                        createTableClickable(itemList, binding.tabLayout2)
                    }
                }
            }
        }
    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        if (data.isNotEmpty()) {
            for (i in 0 until data.size) {
                val tr = TableRow(requireContext())
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
        if (data.isNotEmpty()) {
            for (i in 0 until data.size) {
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
                tr.tag = i
                val c1 = TextView(requireContext())
                c1.gravity = Gravity.START
                c1.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_title
                    )
                )
                c1.text = data[i].first.first
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

                tr.setOnClickListener {
                    pd!!.show()

                    try {
                        soId = data[i].first.second
                        if (sharePrefUtils.getString(Api.USER_TYPE) == "ASM") {

                            binding.progressBarHome.visibility = View.GONE
                            binding.targetLayout.visibility = View.VISIBLE
                            createTable(itemListDetails[i], binding.tabLayoutTarget)
                        } else
                            getSummaryTargets(Api.get_summary + "?today_summary=1&today_for_so=1&user_id=" + data[i].first.second)
                        Log.d("OrderSummary", "row count: " + tabLayout.childCount)
                        for (t in 0 until tabLayout.childCount) {
                            if (tabLayout.getChildAt(t).tag == it.tag) {
                                tabLayout.getChildAt(t)
                                    .setBackgroundColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.light_yellow
                                        )
                                    )
                            } else {
                                tabLayout.getChildAt(t)
                                    .setBackgroundColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.white
                                        )
                                    )
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
        var visitCompleted = "--:--"
        var visitCovered = "--:--"
        val dFormat = DecimalFormat("#.##")
        ApiServices.apiGET(
            url,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val obj = JSONObject(response)
                        val completedArray = obj.getJSONArray("today_summary")
                        binding.progressBarHome.visibility = View.GONE
                        binding.targetLayout.visibility = View.VISIBLE
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("today_ordered_amount")) totalTargetCompleted =
                                    dFormat.format(
                                        targetObj.getString("today_ordered_amount").toDouble()
                                    )
                                if (!targetObj.isNull("sku_per_memo")) bpcCompleted =
                                    dFormat.format(targetObj.getString("sku_per_memo").toDouble())
                                if (!targetObj.isNull("number_of_memo")) lpcCompleted =
                                    dFormat.format(targetObj.getString("number_of_memo").toDouble())
                                if (!targetObj.isNull("number_of_visits")) visitCompleted =
                                    dFormat.format(
                                        targetObj.getString("number_of_visits").toDouble()
                                    )
                                if (!targetObj.isNull("distance_from_outlets")) visitCovered =
                                    dFormat.format(
                                        targetObj.getString("distance_from_outlets").toDouble()
                                    )
                                if (!targetObj.isNull("aiv")) aivCompleted =
                                    dFormat.format(targetObj.getString("aiv").toDouble())
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(
                            Pair(
                                resources.getString(R.string.total_order_value),
                                totalTargetCompleted
                            )
                        )
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
                        itemList.add(Pair(resources.getString(R.string.visit_500m), visitCovered))
                        itemList.add(Pair(resources.getString(R.string.aiv), aivCompleted))

                        createTable(itemList, binding.tabLayoutTarget)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.progressBarHome.visibility = View.GONE
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