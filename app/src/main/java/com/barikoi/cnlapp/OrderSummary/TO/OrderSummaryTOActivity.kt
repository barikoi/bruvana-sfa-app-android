@file:Suppress("DEPRECATION")

package com.barikoi.cnlapp.OrderSummary.TO

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.ProductStock.Model.OrdersSO
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.Order
import com.barikoi.cnlapp.data.remote.models.To
import com.barikoi.cnlapp.databinding.ActivityOrderSummaryToBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.toast
import com.barikoi.cnlapp.utils.extension.totalAmountFormatted
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class OrderSummaryTOActivity : BaseActivity(), OnEditOrderListener {
    private lateinit var binding: ActivityOrderSummaryToBinding

    private val viewModel: OrderSummeryTOViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var queue: RequestQueue


    private val itemListDetails: MutableList<ArrayList<Pair<String, String>>> = mutableListOf()

    private var toList: List<To> = emptyList()

    lateinit var listener: OnEditOrderListener
    private lateinit var adapter: ConfirmOrderListAdapter

    var startDate: String? = null
    var endDate: String? = null

    private var customDate: String? = null
    private var shoWithOrderList: ArrayList<OrdersSO> = ArrayList()
    private var orderArray: JSONArray? = null

    val itemList: ArrayList<OrderList> = ArrayList()

    lateinit var pd: ProgressDialog


    val dFormat = DecimalFormat("#.##")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderSummaryToBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTodaySummaryObserve()

        listener = this

        adapter = ConfirmOrderListAdapter(listener, "summary")
        binding.orderList.layoutManager = LinearLayoutManager(this)
        binding.orderList.adapter = adapter

        pd = ProgressDialog(this)
        pd.setMessage("Processing...")
        pd.setCancelable(false)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editTextSearchShop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(s)
                if (s!!.isEmpty()) {
                    adapter.updateList(itemList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
        binding.tryAgain2.setOnClickListener {
            setDateFilter()
        }

        setDateFilter()
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        startDate = df.format(start)
        endDate = df.format(end)
        customDate =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))
        binding.tvDateRange.text = customDate

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
            viewModel.getTodaySummary(
                startDate!!,
                endDate!!,
                "1"
            )

        } else {
            getOrderSummary(
                Api.get_all_so_list + "?last_week_summary=1&start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&to_id=" + sharePrefUtils.getString(
                    Api.USER_ID
                )
            )
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
            pd.show()
            binding.spinnerMenu.setSelection(2)
            binding.dateRangeLayout.isEnabled = true
            binding.summaryLayout2.visibility = View.GONE
            binding.tryAgain2.visibility = View.GONE
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            startDate = df.format(sDate)
            endDate = df.format(eDate)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = simpleFormat.format(sDate)
                customDate = simpleFormat.format(sDate)
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(sDate),
                    simpleFormat.format(eDate)
                )
                customDate = simpleFormat.format(sDate) + " - " + simpleFormat.format(eDate)
            }

            if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                viewModel.getTodaySummary(
                    startDate!!,
                    endDate!!,
                    "1"
                )

            } else

                getOrderSummary(
                    Api.get_all_so_list + "?start_date=" + df.format(
                        sDate
                    ) + " 00:00:00" + "&end_date=" + df.format(eDate) + " 23:59:59" + "&to_id=" + sharePrefUtils.getString(
                        Api.USER_ID
                    )
                )
        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
        }

    }

    private fun startTodaySummaryObserve() {
        lifecycleScope.launch {
            viewModel.todaySummaryResponse.observe(this@OrderSummaryTOActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startTodaySummaryObserve::Empty")

                        binding.progressBar4.visibility = View.GONE
                        binding.summaryLayout2.visibility = View.GONE
                        binding.tryAgain2.visibility = View.GONE
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startTodaySummaryObserve::Error ${it.error}")

                        binding.bodyLayout.visibility = View.GONE
                        binding.progressBar4.visibility = View.GONE
                        binding.summaryLayout2.visibility = View.GONE
                        binding.targetLayout.visibility = View.GONE
                        binding.bodyLayoutScroll.visibility = View.GONE
                        binding.collectionLayout.visibility = View.GONE
                        binding.tryAgain2.visibility = View.VISIBLE

                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startTodaySummaryObserve::Loading")

                        binding.progressBar4.visibility = View.VISIBLE
                        binding.summaryLayout2.visibility = View.GONE
                        binding.tryAgain2.visibility = View.GONE
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startTodaySummaryObserve:: Success ${it.data}")

                        binding.progressBar4.visibility = View.GONE
                        binding.summaryLayout2.visibility = View.VISIBLE
                        binding.collectionLayout.visibility = View.GONE
                        binding.targetLayout.visibility = View.GONE
                        binding.bodyLayoutScroll.visibility = View.GONE
                        binding.tryAgain2.visibility = View.GONE
                        binding.bodyLayout.visibility = View.VISIBLE

                        toList = it.data?.toList ?: emptyList()

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
                            val aiv = to.totalOrderedAmount.toDoubleOrNull()
                                ?.takeIf { to.totalOrders != 0 }?.div(to.totalOrders) ?: 0.0


                            val itemListDetailsTmp = ArrayList<Pair<String, String>>()
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.total_order_value),
                                    to.totalAmountFormatted()
                                )
                            )
                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.ads),
                                    to.ads.toString().totalAmountFormatted()
                                )
                            )

                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.rds),
                                    to.rds.toString().totalAmountFormatted()
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
                                    resources.getString(R.string.aiv),
                                    to.aiv.toString().totalAmountFormatted()
                                )
                            )

                            itemListDetailsTmp.add(
                                Pair(
                                    resources.getString(R.string.bounce) + " (%)",
                                    to.bouncedPercentage.toString()
                                )
                            )


                            itemListDetails.add(itemListDetailsTmp)
                        }

                        createTableClickable(itemList, binding.tabLayoutOrder)
                    }
                }
            }
        }
    }

    private fun getOrderSummary(url: String) {
        binding.progressBar4.visibility = View.VISIBLE
        ApiServices.apiGET(url, queue, sharePrefUtils.getString(Api.TOKEN)!!, object :
            ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    binding.progressBar4.visibility = View.GONE
                    binding.summaryLayout2.visibility = View.VISIBLE
                    binding.collectionLayout.visibility = View.GONE
                    binding.targetLayout.visibility = View.GONE
                    binding.bodyLayoutScroll.visibility = View.GONE
                    binding.tryAgain2.visibility = View.GONE
                    binding.bodyLayout.visibility = View.VISIBLE
                    val obj = JSONObject(response)
                    val toArray = obj.getJSONArray("so_list")
                    val toObj = toArray.getJSONObject(0)
                    val ordersArray = toObj.getJSONArray("sales_officers")
                    val itemList: ArrayList<Pair<Pair<String, String>, String>> = ArrayList()
                    shoWithOrderList.clear()
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
                                        orderObj.getString("so_ordered_value").toDouble()
                                    )
                                )
                            )
                            shoWithOrderList.add(
                                OrdersSO(
                                    orderObj.getString("id"),
                                    orderObj.getString("user_name"),
                                    orderObj.getString("productive_outlets"),
                                    orderObj.getString("total_outlets"),
                                    dFormat.format(orderObj.getDouble("total_bounced_amount")),
                                    (if (orderObj.has("orders")) orderObj.getJSONArray("orders") else JSONArray())!!
                                )
                            )
                        }
                        if (!toObj.getString("order_amount")
                                .equals("null")
                        ) binding.ovCount.text =
                            dFormat.format(toObj.getString("order_amount").toDouble())
                        if (!toObj.getString("sku_per_memo")
                                .equals("null")
                        ) binding.bpcCount.text =
                            dFormat.format(toObj.getString("sku_per_memo").toDouble())
                        if (!toObj.getString("number_of_memo")
                                .equals("null")
                        ) binding.lpcCount.text =
                            dFormat.format(toObj.getString("number_of_memo").toDouble())
                        createTableClickable(itemList, binding.tabLayoutOrder)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.bodyLayout.visibility = View.GONE
                    binding.progressBar4.visibility = View.GONE
                    binding.summaryLayout2.visibility = View.GONE
                    binding.targetLayout.visibility = View.GONE
                    binding.bodyLayoutScroll.visibility = View.GONE
                    binding.collectionLayout.visibility = View.GONE
                    binding.tryAgain2.visibility = View.VISIBLE
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
                binding.progressBar4.visibility = View.GONE
                binding.summaryLayout2.visibility = View.GONE
                binding.targetLayout.visibility = View.GONE
                binding.collectionLayout.visibility = View.GONE
                binding.bodyLayout.visibility = View.GONE
                binding.bodyLayoutScroll.visibility = View.GONE
                binding.tryAgain2.visibility = View.VISIBLE
            }

            override fun onException(e: Exception) {
                binding.progressBar4.visibility = View.GONE
                binding.summaryLayout2.visibility = View.GONE
                binding.collectionLayout.visibility = View.GONE
                binding.bodyLayoutScroll.visibility = View.GONE
                binding.tryAgain2.visibility = View.VISIBLE
            }

        })
    }

    private fun createTableClickable(
        data: ArrayList<Pair<Pair<String, String>, String>>,
        tabLayout: TableLayout
    ) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()

        if (data.isNotEmpty()) {
            for (i in 0 until data.size) {
                val tr = TableRow(applicationContext)
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
                val c1 = TextView(applicationContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.text = data[i].first.first
                val c2 = TextView(applicationContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.text = data[i].second
                c2.gravity = Gravity.CENTER
                c2.background = ContextCompat.getDrawable(this, R.drawable.button_white_bg_stroke)
                tr.addView(c1)
                tr.addView(c2)

                tr.setOnClickListener {
                    binding.bodyLayoutScroll.visibility = View.GONE
                    binding.collectionLayout.visibility = View.GONE
                    binding.targetLayout.visibility = View.GONE
                    binding.orderList.visibility = View.GONE
                    pd.show()
                    Thread {
                        this@OrderSummaryTOActivity.runOnUiThread {
                            try {
                                if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                                    binding.bodyLayoutScroll.visibility = View.VISIBLE
                                    binding.collectionLayout.visibility = View.VISIBLE
                                    binding.orderCollectionCount.text =
                                        getString(
                                            R.string.by_das_by,
                                            toList[i].totalOrders.toString(),
                                            toList[i].uniqueOutletCount.toString()
                                        )
                                    binding.totalBounceCount.text =
                                        toList[i].totalBouncedAmount

                                    createTable(itemListDetails[i], binding.tabLayoutTarget)
                                    binding.targetLayout.visibility = View.VISIBLE

                                    for (t in 0 until tabLayout.childCount) {
                                        if (tabLayout.getChildAt(t).tag == it.tag) {
                                            tabLayout.getChildAt(t)
                                                .setBackgroundColor(resources.getColor(R.color.light_yellow))
                                        } else {
                                            tabLayout.getChildAt(t)
                                                .setBackgroundColor(resources.getColor(R.color.white))
                                        }
                                    }

                                } else {
                                    binding.bodyLayoutScroll.visibility = View.VISIBLE
                                    binding.collectionLayout.visibility = View.VISIBLE
                                    binding.orderCollectionCount.text =
                                        getString(
                                            R.string.by_das_by,
                                            shoWithOrderList[i].order_collected,
                                            shoWithOrderList[i].total_outlets
                                        )
                                    binding.totalBounceCount.text =
                                        shoWithOrderList[i].total_bounce

                                    getSummaryTargets(Api.get_summary + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&user_id=" + data[i].first.second/*+"&route_id="+routeId*/)

                                    for (t in 0 until tabLayout.childCount) {
                                        if (tabLayout.getChildAt(t).tag == it.tag) {
                                            tabLayout.getChildAt(t)
                                                .setBackgroundColor(resources.getColor(R.color.light_yellow))
                                        } else {
                                            tabLayout.getChildAt(t)
                                                .setBackgroundColor(resources.getColor(R.color.white))
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Sentry.captureException(e)
                            }
                        }
                    }.start()
                }
                tabLayout.addView(tr)
            }
        }
        pd.dismiss()
    }

    private fun getAllOrders(orderArray: JSONArray) {
        try {
            itemList.clear()
            if (orderArray.length() > 0) {
                for (i in 0 until orderArray.length()) {
                    val orderObj = orderArray.getJSONObject(i)
                    val brandArray = orderObj.getJSONArray("products")
                    val productItems: ArrayList<Products> = ArrayList()
                    if (brandArray.length() > 0) {
                        for (j in 0 until brandArray.length()) {
                            val brandObj = brandArray.getJSONObject(j)
                            productItems.add(
                                Products(
                                    brandObj.getString("product_id"),
                                    brandObj.getString("product_name"),
                                    brandObj.getString("product_code"),
                                    brandObj.getDouble("unit_price"),
                                    brandObj.getDouble("discounted_unit_price"),
                                    brandObj.getString("sku_code"),
                                    /*0.0,*/ "",
                                    brandObj.getString("unit_id"),
                                    brandObj.getString("unit_name"),
                                    brandObj.getString("unit_code"),
                                    brandObj.getString("category_id"),
                                    brandObj.getString("category_name"),
                                    brandObj.getString("category_code"), 0, 0,
                                    brandObj.getInt("bounced_quantity"),
                                    brandObj.getInt("ordered_quantity"),
                                    brandObj.getDouble("ordered_amount")
                                )
                            )
                        }
                    }
                    itemList.add(
                        OrderList(
                            null,
                            orderObj.getString("order_no"),
                            orderObj.getString("ordered_at"),
                            orderObj.getString("order_status"),
                            orderObj.getString("outlet_id"),
                            orderObj.getString("outlet_name"),
                            "",
                            "",
                            orderObj.getString("total_ordered_amount"),
                            orderObj.getString("total_ordered_quantity"),
                            orderObj.getString("latitude"),
                            orderObj.getString("longitude"),
                            orderObj.getString("distance_from_outlets"),
                            productItems
                        )
                    )
                }
            }
            binding.orderList.visibility = View.VISIBLE
            adapter.updateList(itemList)

            binding.orderList
                .viewTreeObserver
                .addOnGlobalLayoutListener(
                    object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            pd.dismiss()
                            binding.orderList
                                .viewTreeObserver
                                .removeOnGlobalLayoutListener(this)
                        }
                    })


        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        ApiServices.apiGET(
            url,
            queue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val obj = JSONObject(response)
                        obj.getJSONArray("targets")
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
                        pd.dismiss()
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                    binding.progressBarHome.visibility = View.GONE
                    pd.dismiss()
                }

                override fun onException(e: Exception) {
                    binding.progressBarHome.visibility = View.GONE
                    pd.dismiss()
                }

            })

    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        if (data.isNotEmpty()) {
            binding.bodyLayoutScroll.smoothScrollTo(0, 0)
            for (i in 0 until data.size) {
                val tr = TableRow(applicationContext)
                val c1 = TextView(applicationContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.text = data[i].first
                val c2 = TextView(applicationContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.text = data[i].second
                tr.addView(c1)
                tr.addView(c2)
                tabLayout.addView(tr)
            }
        }
        pd.dismiss()
    }

    override fun onEdit(order: OrderList) {

    }
}