package com.barikoi.cnlapp.OrderSummary.SO

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.DialogListener
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.activity_order_summary.btnBack
import kotlinx.android.synthetic.main.activity_order_summary.tvRouteName
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class OrderSummaryActivity : AppCompatActivity(), OnEditOrderListener {

    var token : String? = null
    var user_id : String? = null
    var sr_id : String? = null
    var route_id: String? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var listener : OnEditOrderListener? =null
    private var adapter: ConfirmOrderListAdapter? = null
    var StartDate: String? = null
    var EndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        sr_id = prefs!!.getString(Api.SR_CODE, "")
        route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

        listener = this
        if (route_id!!.length > 0) {
            setDateFilter()
            no_route_check.visibility =View.GONE
            bodyLayout.visibility = View.VISIBLE
        }else{
            //setDateFilter()
            /*ViewUtils.viewDialogResponse(applicationContext, resources.getString(R.string.no_route_selected_today), object : DialogListener{
                override fun onConfirmed() {
                    TODO("Not yet implemented")
                }

                override fun onCanceled() {
                    TODO("Not yet implemented")
                }

            })*/

            no_route_check.visibility =View.VISIBLE
            bodyLayout.visibility = View.GONE
            btn_tryAgain.setOnClickListener {
                setDateFilter()
            }
        }

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
                    getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+StartDate+" 00:00:00"+"&end_date="+ EndDate+" 23:59:59"+"&order_status=PENDING, DELIVERED")
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })
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
            getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+df.format(s_date)+" 00:00:00"+"&end_date="+df.format(e_date)+" 23:59:59"+"&order_status=PENDING, DELIVERED")

        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

        getAllOrders(Api.get_saved_order+"?sr_id="+sr_id+"&route_id="+route_id+"&start_date="+StartDate+" 00:00:00"+"&end_date="+ EndDate+" 23:59:59"+"&order_status=PENDING, DELIVERED")
    }

    private fun getAllOrders(url: String) {
        progressBarOrder.visibility = View.VISIBLE
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        progressBarOrder.visibility = View.GONE
                        val itemList: ArrayList<OrderList> = ArrayList()

                        val obj = JSONObject(response)
                        val orderArray = obj.getJSONArray("orders")
                        if (orderArray.length() > 0){
                            for (i in 0 until orderArray.length()){
                                //productItems.clear()
                                val orderObj = orderArray.getJSONObject(i)
                                val brandArray = orderObj.getJSONArray("brands")
                                tvRouteName.setText(orderObj.getString("route_name"))
                                val productItems: ArrayList<Products> = ArrayList()
                                if (brandArray.length() > 0){
                                    for (j in 0 until brandArray.length()){
                                        val brandObj = brandArray.getJSONObject(j)
                                        if(brandObj.getInt("quantity") > 0) {
                                            productItems.add(
                                                Products(
                                                    brandObj.getString("product_id"),
                                                    brandObj.getString("product"),
                                                    "",
                                                    brandObj.getString("brand_id"),
                                                    "",
                                                    brandObj.getDouble("unit_price"),
                                                    0.0, "",
                                                    brandObj.getString("unit_name"),
                                                    "", 0, 0,
                                                    brandObj.getInt("bounce"),
                                                    brandObj.getInt("quantity"),
                                                    brandObj.getDouble("total_price")
                                                )
                                            )
                                        }
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
                                        orderObj.getString("route_id"),
                                        orderObj.getString("route_name"),
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


                    }
                }catch (e: Exception){
                    progressBarOrder.visibility = View.GONE
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
                progressBarOrder.visibility = View.GONE
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                progressBarOrder.visibility = View.GONE
            }

        })
    }

    override fun onEdit(order: OrderList) {
        TODO("Not yet implemented")
    }

}