package com.barikoi.cnlapp.OrderSummary.TO

import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.preference.PreferenceManager
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
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_order_summary_to.bodyLayout
import kotlinx.android.synthetic.main.activity_order_summary_to.bodyLayoutScroll
import kotlinx.android.synthetic.main.activity_order_summary_to.bpcCount
import kotlinx.android.synthetic.main.activity_order_summary_to.btnBack
import kotlinx.android.synthetic.main.activity_order_summary_to.collectionLayout
import kotlinx.android.synthetic.main.activity_order_summary_to.dateRangeLayout
import kotlinx.android.synthetic.main.activity_order_summary_to.editTextSearchShop
import kotlinx.android.synthetic.main.activity_order_summary_to.lpcCount
import kotlinx.android.synthetic.main.activity_order_summary_to.orderList
import kotlinx.android.synthetic.main.activity_order_summary_to.order_collection_count
import kotlinx.android.synthetic.main.activity_order_summary_to.ovCount
import kotlinx.android.synthetic.main.activity_order_summary_to.progressBar4
import kotlinx.android.synthetic.main.activity_order_summary_to.progressBarHome
import kotlinx.android.synthetic.main.activity_order_summary_to.spinnerMenu
import kotlinx.android.synthetic.main.activity_order_summary_to.summaryLayout2
import kotlinx.android.synthetic.main.activity_order_summary_to.tabLayoutOrder
import kotlinx.android.synthetic.main.activity_order_summary_to.tabLayoutTarget
import kotlinx.android.synthetic.main.activity_order_summary_to.targetLayout
import kotlinx.android.synthetic.main.activity_order_summary_to.total_bounce_count
import kotlinx.android.synthetic.main.activity_order_summary_to.tryAgain2
import kotlinx.android.synthetic.main.activity_order_summary_to.tvDateRange
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class OrderSummaryTOActivity : BaseActivity(), OnEditOrderListener {
    var token: String? = null
    var user_id: String? = null
    var sr_id: String? = null
    var route_id: String? = null
    var employeeId: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var listener: OnEditOrderListener? = null
    private var adapter: ConfirmOrderListAdapter? = null
    var StartDate: String? = null
    var EndDate: String? = null
    var customDate: String? = null
    var sowithOrderList: ArrayList<OrdersSO>? = ArrayList()
    var orderArray: JSONArray? = null
    val itemList: ArrayList<OrderList> = ArrayList()
    var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary_to)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")

        listener = this
        pd = ProgressDialog(this)
        pd!!.setMessage("Processing...")
        pd!!.setCancelable(false)

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        editTextSearchShop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    adapter = ConfirmOrderListAdapter(itemList, listener!!, "summary")
                    orderList.adapter = adapter
                    adapter!!.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        tryAgain2.setOnClickListener {
            setDateFilter(/*spinnerMenu.selectedItemPosition*/)
        }

        setDateFilter(/*spinnerMenu.selectedItemPosition*/)

    }

    private fun setDateFilter(/*position: Int*/) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        StartDate = df.format(start)
        EndDate = df.format(end)
        customDate =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))
        tvDateRange.text = customDate
        getOrderSummary(Api.get_all_so_list + "?last_week_summary=1&start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&to_id=" + user_id)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            pd!!.show()
            spinnerMenu.setSelection(2)
            dateRangeLayout.setEnabled(true)
            summaryLayout2.visibility = View.GONE
            tryAgain2.visibility = View.GONE
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            StartDate = df.format(s_date)
            EndDate = df.format(e_date)
            //spinnerMenu.setSelection(2)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.text = simpleFormat.format(s_date)
                customDate = simpleFormat.format(s_date)
            } else {
                tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(s_date),
                    simpleFormat.format(e_date)
                )
                customDate = simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date)
            }

            getOrderSummary(
                Api.get_all_so_list + "?start_date=" + df.format(
                    s_date
                ) + " 00:00:00" + "&end_date=" + df.format(e_date) + " 23:59:59" + "&to_id=" + user_id
            )
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

    }

    private fun getOrderSummary(url: String) {
        progressBar4.visibility = View.VISIBLE
        val dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, queue!!, token!!, object :
            ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null) {
                        progressBar4.visibility = View.GONE
                        summaryLayout2.visibility = View.VISIBLE
                        collectionLayout.visibility = View.GONE
                        targetLayout.visibility = View.GONE
                        bodyLayoutScroll.visibility = View.GONE
                        tryAgain2.visibility = View.GONE
                        bodyLayout.visibility = View.VISIBLE
                        val obj = JSONObject(response)
                        val toArray = obj.getJSONArray("so_list")
                        val toObj = toArray.getJSONObject(0)
                        val ordersArray = toObj.getJSONArray("sales_officers")
                        val itemList: ArrayList<Pair<Pair<String, String>, String>> = ArrayList()
                        sowithOrderList!!.clear()
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
                                            orderObj.getString("so_ordered_value").toDouble()
                                        )
                                    )
                                )
                                sowithOrderList!!.add(
                                    OrdersSO(
                                        orderObj.getString("id"),
                                        orderObj.getString("user_name"),
                                        orderObj.getString("productive_outlets"),
                                        orderObj.getString("total_outlets"),
                                        dformat.format(orderObj.getDouble("total_bounced_amount")),
                                        (if(orderObj.has("orders")) orderObj.getJSONArray("orders") else JSONArray())!!
                                    )
                                )
                            }
                            if (!toObj.getString("order_amount").equals("null")) ovCount.setText(
                                dformat.format(toObj.getString("order_amount").toDouble())
                            )
                            if (!toObj.getString("sku_per_memo").equals("null")) bpcCount.setText(
                                dformat.format(toObj.getString("sku_per_memo").toDouble())
                            )
                            if (!toObj.getString("number_of_memo").equals("null")) lpcCount.setText(
                                dformat.format(toObj.getString("number_of_memo").toDouble())
                            )
                            createTableClickable(itemList, tabLayoutOrder)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    bodyLayout.visibility = View.GONE
                    progressBar4.visibility = View.GONE
                    summaryLayout2.visibility = View.GONE
                    targetLayout.visibility = View.GONE
                    bodyLayoutScroll.visibility = View.GONE
                    collectionLayout.visibility = View.GONE
                    tryAgain2.visibility = View.VISIBLE
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
                progressBar4.visibility = View.GONE
                summaryLayout2.visibility = View.GONE
                targetLayout.visibility = View.GONE
                collectionLayout.visibility = View.GONE
                bodyLayout.visibility = View.GONE
                bodyLayoutScroll.visibility = View.GONE
                tryAgain2.visibility = View.VISIBLE
            }

            override fun onException(e: Exception) {
                progressBar4.visibility = View.GONE
                summaryLayout2.visibility = View.GONE
                collectionLayout.visibility = View.GONE
                bodyLayoutScroll.visibility = View.GONE
                tryAgain2.visibility = View.VISIBLE
            }

        })
    }

    private fun createTableClickable(
        data: ArrayList<Pair<Pair<String, String>, String>>,
        tab_Layout: TableLayout
    ) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()

        if (data.size > 0) {
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
                tr.setLayoutParams(tableRowParams)
                tr.gravity = Gravity.CENTER_VERTICAL
                tr.tag = i
                val c1 = TextView(applicationContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.setText(data.get(i).first.first)
                val c2 = TextView(applicationContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.setText(data.get(i).second)
                c2.gravity = Gravity.CENTER
                c2.background = resources.getDrawable(R.drawable.button_white_bg_stroke)
                tr.addView(c1)
                tr.addView(c2)

                tr.setOnClickListener {
                    Log.d("OrderSummary", "pd.isShowing: " + pd!!.isShowing)
                    bodyLayoutScroll.visibility = View.GONE
                    collectionLayout.visibility = View.GONE
                    targetLayout.visibility = View.GONE
                    orderList.visibility = View.GONE
                    pd!!.show()
                    Thread {
                        this@OrderSummaryTOActivity.runOnUiThread(object : Runnable {
                            override fun run() {
                                try {
                                    orderArray = sowithOrderList!!.get(i).ordersArray
                                    bodyLayoutScroll.visibility = View.VISIBLE
                                    collectionLayout.visibility = View.VISIBLE
                                    order_collection_count.text = sowithOrderList!!.get(i).order_collected + "/" + sowithOrderList!!.get(
                                        i
                                    ).total_outlets
                                    total_bounce_count.setText(sowithOrderList!!.get(i).total_bounce.toString())
                                    getSummaryTargets(Api.get_summary + "?start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&user_id=" + data[i].first.second/*+"&route_id="+routeId*/)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Log.d("OrderSummary", "row count: " + tab_Layout.childCount)
                                        for (t in 0 until tab_Layout.childCount) {
                                            if (tab_Layout.getChildAt(t).tag == it.tag) {
                                                tab_Layout.getChildAt(t)
                                                    .setBackgroundColor(resources.getColor(R.color.light_yellow))
                                            } else {
                                                tab_Layout.getChildAt(t)
                                                    .setBackgroundColor(resources.getColor(R.color.white))
                                            }
                                        }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Sentry.captureException(e)
                                }
                            }

                        })
                    }.start()


                }
                tab_Layout.addView(tr)
            }
        }
        pd!!.dismiss()
    }

    private fun getAllOrders(orderArray: JSONArray) {
        //progressBar5.visibility = View.GONE
        try {
            itemList.clear()
            if (orderArray.length() > 0) {
                for (i in 0 until orderArray.length()) {
                    //productItems.clear()
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
                            /*orderObj.getString("distributor_office_code"),*/
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
            orderList.visibility = View.VISIBLE
            adapter = ConfirmOrderListAdapter(itemList, listener!!, "summary")
            orderList.adapter = adapter
            adapter!!.notifyDataSetChanged()

            orderList
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                    object : OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            // At this point the layout is complete and the
                            // dimensions of recyclerView and any child views
                            // are known.
                            pd!!.dismiss()
                            orderList
                                .getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this)
                        }
                    })


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSummaryTargets(url: String) {
        //pd!!.show()
        //progressBarHome.visibility = View.VISIBLE
        var total_target = "--:--"
        var total_target_completed = "--:--"
        var lpc = "--:--"
        var lpc_completed = "--:--"
        var bpc = "--:--"
        var bpc_completed = "--:--"
        var aiv = "--:--"
        var aiv_completed = "--:--"
        var ads = "--:--"
        var ads_completed = "--:--"
        var rds = "--:--"
        var rds_completed = "--:--"
        var visit_completed = "--:--"
        var visited = "--:--"
        var bounce_completed = "--:--"
        var bounced = "--:--"
        var delivery_value = "--:--"
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null) {
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        progressBarHome.visibility = View.GONE
                        targetLayout.visibility = View.VISIBLE
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("revenue")) total_target_completed =
                                    dformat.format(targetObj.getString("revenue").toDouble())
                                if (!targetObj.isNull("ads")) ads_completed =
                                    dformat.format(targetObj.getString("ads").toDouble())
                                if (!targetObj.isNull("rds")) rds_completed =
                                    dformat.format(targetObj.getString("rds").toDouble())
                                if (!targetObj.isNull("sku_per_memo")) bpc_completed =
                                    dformat.format(targetObj.getString("sku_per_memo").toDouble())
                                if (!targetObj.isNull("number_of_memo")) lpc_completed =
                                    dformat.format(targetObj.getString("number_of_memo").toDouble())
                                if (!targetObj.isNull("number_of_visits")) visit_completed =
                                    dformat.format(
                                        targetObj.getString("number_of_visits").toDouble()
                                    )
                                if (!targetObj.isNull("aiv")) aiv_completed =
                                    dformat.format(targetObj.getString("aiv").toDouble())
                                if (!targetObj.isNull("bounce_amount_percentage")) bounce_completed =
                                    dformat.format(
                                        targetObj.getString("bounce_amount_percentage").toDouble()
                                    )
                                if (!targetObj.isNull("delivered_value")) delivery_value =
                                    dformat.format(
                                        targetObj.getString("delivered_value").toDouble()
                                    )
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(
                            Pair(
                                resources.getString(R.string.total_delivery_value),
                                total_target_completed
                            )
                        )
                        itemList.add(Pair(resources.getString(R.string.ads), ads_completed))
                        itemList.add(Pair(resources.getString(R.string.rds), rds_completed))
                        itemList.add(
                            Pair(
                                resources.getString(R.string.sku_per_memo),
                                bpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.number_of_memo),
                                lpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.visit_ratio),
                                visit_completed
                            )
                        )
                        itemList.add(Pair(resources.getString(R.string.aiv), aiv_completed))
                        itemList.add(
                            Pair(
                                resources.getString(R.string.bounce) + " (%)",
                                bounce_completed
                            )
                        )
                        //itemList.add(Pair(resources.getString(R.string.delivery_value), delivery_value))

                        createTable(itemList, tabLayoutTarget)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBarHome.visibility = View.GONE
                    //getAllOrders(orderArray!!)
                    pd!!.dismiss()
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
                progressBarHome.visibility = View.GONE
                //getAllOrders(orderArray!!)
                pd!!.dismiss()
            }

            override fun onException(e: Exception) {
                progressBarHome.visibility = View.GONE
                //getAllOrders(orderArray!!)
                pd!!.dismiss()
            }

        })

    }

    private fun createTable(data: ArrayList<Pair<String, String>>, tabLayout: TableLayout) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        tabLayout.removeAllViews()
        if (data.size > 0) {
            //bodyLayoutScroll.scrollTo(0, 0)
            bodyLayoutScroll.smoothScrollTo(0, 0)
            for (i in 0 until data.size) {
                val tr = TableRow(applicationContext)
                val c1 = TextView(applicationContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.setText(data.get(i).first)
                val c2 = TextView(applicationContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.setText(data.get(i).second)
                tr.addView(c1)
                tr.addView(c2)
                tabLayout.addView(tr)
            }
        }
        //getAllOrders(orderArray!!)
        pd!!.dismiss()
    }

    override fun onEdit(order: OrderList) {

    }

    /*override fun onClick(v: View?) {
        Log.d("OrderSummary", "clicked: "+v!!.id)
    }*/


}