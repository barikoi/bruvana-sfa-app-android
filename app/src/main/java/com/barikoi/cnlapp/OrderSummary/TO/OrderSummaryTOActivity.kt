package com.barikoi.cnlapp.OrderSummary.TO

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.ProductStock.Model.OrdersSO
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_order_summary_to.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderSummaryTOActivity : AppCompatActivity(), OnEditOrderListener {
    var token : String? = null
    var user_id : String? = null
    var sr_id : String? = null
    var route_id: String? = null
    var employeeId: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var listener : OnEditOrderListener? =null
    private var adapter: ConfirmOrderListAdapter? = null
    var StartDate: String? = null
    var EndDate: String? = null
    var sowithOrderList: ArrayList<OrdersSO>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary_to)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        /*sr_id = prefs!!.getString(Api.SR_CODE, "")
        route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")*/
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")

        listener = this
        setDateFilter()

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        editTextSearchShop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0){
                    //getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+StartDate+"&end_date="+EndDate+"&with_summary=1")
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })
        tryAgain2.setOnClickListener {
            setDateFilter()
        }
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        StartDate = df.format(start)
        EndDate = df.format(end)

        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                /*editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()*/
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                /*editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()*/
            }
            //getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+"&with_summary=1")
            getOrderSummary(Api.get_all_so_list+"?last_week_summary=1&start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+"&to="+employeeId)
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

        getOrderSummary(Api.get_all_so_list+"?last_week_summary=1&start_date="+StartDate+"&end_date="+EndDate+"&to="+employeeId)
        //getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+StartDate+"&end_date="+EndDate+"&with_summary=1")
    }

    fun getOrderSummary(url: String){
        progressBar4.visibility = View.VISIBLE
        val dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, queue!!, token!!, object :
            ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        progressBar4.visibility = View.GONE
                        summaryLayout2.visibility = View.VISIBLE
                        tryAgain2.visibility = View.GONE
                        val obj = JSONObject(response)
                        val ordersArray = obj.getJSONArray("so_list")
                        val itemList: ArrayList<Pair<Pair<String, String>, String>> = ArrayList()
                        sowithOrderList!!.clear()
                        if (ordersArray.length() >0){
                            for(i in 0 until ordersArray.length()){
                                val orderObj = ordersArray.getJSONObject(i)
                                itemList.add(Pair(Pair(orderObj.getString("sr_name"),orderObj.getString("sr_id")), orderObj.getString("productive_outlets")))
                                sowithOrderList!!.add(
                                    OrdersSO(
                                        orderObj.getString("sr_id"),
                                        orderObj.getString("sr_name"),
                                        orderObj.getString("productive_outlets"),
                                        orderObj.getString("total_outlets"),
                                        dformat.format(orderObj.getDouble("bounce_amount")).toDouble(),
                                        orderObj.getJSONArray("orders")
                                    )
                                )
                            }
                            if (!obj.getString("order_amount").equals("null")) ovCount.setText(dformat.format(obj.getString("order_amount").toDouble()))
                            if (!obj.getString("sku_per_memo").equals("null")) bpcCount.setText(dformat.format(obj.getString("sku_per_memo").toDouble()))
                            if (!obj.getString("number_of_memo").equals("null")) lpcCount.setText(dformat.format(obj.getString("number_of_memo").toDouble()))
                            createTable(itemList, tabLayoutOrder)
                        }

                    }
                }catch (e: Exception){
                    e.printStackTrace()
                    progressBar4.visibility = View.GONE
                    tryAgain2.visibility = View.VISIBLE
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
                progressBar4.visibility = View.GONE
                tryAgain2.visibility = View.VISIBLE
            }

            override fun onException(e: Exception) {
                progressBar4.visibility = View.GONE
                tryAgain2.visibility = View.VISIBLE
            }

        })
    }
    private fun createTable(data: ArrayList<Pair<Pair<String, String>, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        for (i in 0 until /*data.size*/5) {
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
            c1.setText(data.get(i).first.first)
            val c2 = TextView(applicationContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.gravity = Gravity.CENTER
            c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            //tr.setOnClickListener(this@OrderSummaryTOActivity)
            tr.setOnClickListener {
                //Log.d("OrderSummary", "clicked: "+i)
                collectionLayout.visibility = View.VISIBLE
                order_collection_count.setText(sowithOrderList!!.get(i).order_collected+"/"+sowithOrderList!!.get(i).total_outlets)
                total_bounce_count.setText(sowithOrderList!!.get(i).total_bounce.toString())
                getAllOrders(sowithOrderList!!.get(i).ordersArray)
            }
            tab_Layout.addView(tr)
        }
    }
    private fun getAllOrders(orderArray: JSONArray) {

        try {
                val itemList: ArrayList<OrderList> = ArrayList()
                if (orderArray.length() > 0){
                    for (i in 0 until orderArray.length()){
                        //productItems.clear()
                        val orderObj = orderArray.getJSONObject(i)
                        val brandArray = orderObj.getJSONArray("brands")
                        val productItems: ArrayList<Products> = ArrayList()
                        if (brandArray.length() > 0){
                            for (j in 0 until brandArray.length()){
                                val brandObj = brandArray.getJSONObject(j)
                                productItems.add(
                                    Products(
                                        brandObj.getString("product_id"),
                                        brandObj.getString("product"),
                                        "",
                                        brandObj.getString("brand_id"),
                                        "",
                                        brandObj.getDouble("unit_price"),
                                        0.0,"",
                                        brandObj.getString("unit_name"),
                                        "",0,0,
                                        brandObj.getInt("bounce"),
                                        brandObj.getInt("quantity"),
                                        brandObj.getDouble("total_price")
                                    )
                                )
                            }

                        }
                        itemList.add(
                            OrderList(
                                null,
                                orderObj.getString("order_no"),
                                orderObj.getString("ordered_at"),
                                orderObj.getString("orders_status"),
                                orderObj.getString("outlet_id"),
                                orderObj.getString("outlet_name"),
                                "",
                                "",
                                orderObj.getString("distributor_office_code"),
                                orderObj.getString("grand_total"),
                                orderObj.getString("latitude"),
                                orderObj.getString("longitude"),
                                productItems
                            )
                        )
                    }
                }


                adapter = ConfirmOrderListAdapter(itemList, listener!!, "summary")
                orderList.adapter = adapter
                adapter!!.notifyDataSetChanged()

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onEdit(order: OrderList) {
        TODO("Not yet implemented")
    }

    /*override fun onClick(v: View?) {
        Log.d("OrderSummary", "clicked: "+v!!.id)
    }*/


}