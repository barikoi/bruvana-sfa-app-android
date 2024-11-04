package com.barikoi.cnlapp.OrderSummary.SO

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.Order_Create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.Order_Create.RoomDB.OrderList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.databinding.ActivityOrderSummaryBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrderSummaryActivity : BaseActivity(), OnEditOrderListener {
    private lateinit var binding: ActivityOrderSummaryBinding

    var token: String? = null
    var user_id: String? = null
    var sr_id: String? = null
    var route_id: String? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var listener: OnEditOrderListener? = null
    private var adapter: ConfirmOrderListAdapter? = null
    var StartDate: String? = null
    var EndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.noRouteCheck.visibility = View.GONE
            binding.bodyLayout.visibility = View.VISIBLE
        } else {
            //setDateFilter()
            /*ViewUtils.viewDialogResponse(applicationContext, resources.getString(R.string.no_route_selected_today), object : DialogListener{
                override fun onConfirmed() {
                    TODO("Not yet implemented")
                }

                override fun onCanceled() {
                    TODO("Not yet implemented")
                }

            })*/
            binding.progressBar.visibility = View.GONE
            binding.noRouteCheck.visibility = View.VISIBLE
            binding.bodyLayout.visibility = View.GONE
            binding.btnTryAgain.setOnClickListener {
                setDateFilter()
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.editTextSearchShop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0) {
                    getAllOrders(Api.get_saved_order + "?user_id=" + user_id +/*"&route_id="+route_id+*/"&start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&order_status=PENDING, DELIVERED")
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
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        StartDate = df.format(start)
        EndDate = df.format(end)

        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding. dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.setEnabled(false)
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                binding.tvDateRange.text = simpleFormat.format(s_date)
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(s_date),
                    simpleFormat.format(e_date)
                )
            }
            getAllOrders(
                Api.get_saved_order + "?user_id=" + user_id +/*"&route_id="+route_id+*/"&start_date=" + df.format(
                    s_date
                ) + " 00:00:00" + "&end_date=" + df.format(e_date) + " 23:59:59" + "&order_status=PENDING, DELIVERED"
            )

        }

        materialDatePicker.addOnNegativeButtonClickListener { binding.dateRangeLayout.setEnabled(true) }

        getAllOrders(Api.get_saved_order + "?user_id=" + user_id +/*"&route_id="+route_id+*/"&start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&order_status=PENDING, DELIVERED")
    }

    private fun getAllOrders(url: String) {
        binding.progressBar.visibility = View.GONE
        binding.progressBarOrder.visibility = View.VISIBLE
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    binding.progressBar.visibility = View.GONE
                    if (response != null) {
                        binding.progressBarOrder.visibility = View.GONE
                        val itemList: ArrayList<OrderList> = ArrayList()

                        val obj = JSONObject(response)
                        val orderArray = obj.getJSONArray("orders")
                        if (orderArray.length() > 0) {
                            for (i in 0 until orderArray.length()) {
                                //productItems.clear()
                                val orderObj = orderArray.getJSONObject(i)
                                val brandArray = orderObj.getJSONArray("products")
                                binding.tvRouteName.text = orderObj.getString("route_name")
                                val productItems: ArrayList<Products> = ArrayList()
                                if (orderObj.getString("order_status").equals("DELIVERED", true)) {
                                    if (brandArray.length() > 0) {
                                        for (j in 0 until brandArray.length()) {
                                            val brandObj = brandArray.getJSONObject(j)
                                            if (brandObj.getInt("delivered_quantity") > 0) {
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
                                                        brandObj.getString("category_code"),
                                                        0, 0,
                                                        brandObj.getInt("bounced_quantity"),
                                                        brandObj.getInt("delivered_quantity"),
                                                        brandObj.getDouble("delivered_amount")
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
                                            orderObj.getString("order_status"),
                                            orderObj.getString("outlet_id"),
                                            orderObj.getString("outlet_name"),
                                            orderObj.getString("route_id"),
                                            orderObj.getString("route_name"),
                                            /*orderObj.getString("distributor_office_code"),*/
                                            orderObj.getString("total_delivered_amount"),
                                            orderObj.getString("total_delivered_quantity"),
                                            orderObj.getString("latitude"),
                                            orderObj.getString("longitude"),
                                            orderObj.getString("distance_from_outlets"),
                                            productItems
                                        )
                                    )
                                } else {
                                    if (brandArray.length() > 0) {
                                        for (j in 0 until brandArray.length()) {
                                            val brandObj = brandArray.getJSONObject(j)
                                            if (brandObj.getInt("ordered_quantity") > 0) {
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
                                                        brandObj.getString("category_code"),
                                                        0, 0,
                                                        brandObj.getInt("bounced_quantity"),
                                                        brandObj.getInt("ordered_quantity"),
                                                        brandObj.getDouble("ordered_amount")
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
                                            orderObj.getString("order_status"),
                                            orderObj.getString("outlet_id"),
                                            orderObj.getString("outlet_name"),
                                            orderObj.getString("route_id"),
                                            orderObj.getString("route_name"),
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
                        }
                        itemList.sortByDescending {
                            it.orderId
                        }

                        adapter = ConfirmOrderListAdapter(itemList, listener!!, "summary")
                        binding.orderList.adapter = adapter
                        adapter!!.notifyDataSetChanged()


                    }
                } catch (e: Exception) {
                    binding.progressBarOrder.visibility = View.GONE
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
                binding.progressBarOrder.visibility = View.GONE
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                binding.progressBarOrder.visibility = View.GONE
            }

        })
    }

    override fun onEdit(order: OrderList) {
        TODO("Not yet implemented")
    }

}