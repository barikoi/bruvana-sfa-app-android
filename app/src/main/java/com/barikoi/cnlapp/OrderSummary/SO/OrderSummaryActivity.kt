package com.barikoi.cnlapp.OrderSummary.SO

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.data.remote.models.offer.Offer
import com.barikoi.cnlapp.databinding.ActivityOrderSummaryBinding
import com.barikoi.cnlapp.order_create.Adapter.ConfirmOrderListAdapter
import com.barikoi.cnlapp.order_create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.order_create.RoomDB.OrderList
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.fromJson
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class OrderSummaryActivity : BaseActivity(), OnEditOrderListener {
    private lateinit var binding: ActivityOrderSummaryBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var queue: RequestQueue

    lateinit var listener: OnEditOrderListener
    private lateinit var adapter: ConfirmOrderListAdapter

    var startDate: String? = null
    var endDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOrderSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.tvTitle.text = getString(R.string.order_summary)

        binding.toolbar.btnBack.setHapticClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        listener = this
        adapter = ConfirmOrderListAdapter(listener, "summary")
        binding.orderList.layoutManager = LinearLayoutManager(applicationContext)
        binding.orderList.adapter = adapter

        if (sharePrefUtils.getString(Api.SELECTED_ROUTE_ID)!!.isNotEmpty()) {
            setDateFilter()
            binding.noRouteCheck.visibility = View.GONE
            binding.bodyLayout.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.noRouteCheck.visibility = View.VISIBLE
            binding.bodyLayout.visibility = View.GONE
            binding.btnTryAgain.setOnClickListener {
                setDateFilter()
            }
        }

        binding.editTextSearchShop.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(s)
                if (s!!.isEmpty()) {
                    getAllOrders(Api.get_saved_order + "?user_id=" + sharePrefUtils.getString(Api.USER_ID) +/*"&route_id="+route_id+*/"&start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&order_status=PENDING, DELIVERED")
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
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

        binding.tvDateRange.text =
            getString(R.string.date_range_, simpleFormat.format(start), simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText(getString(R.string.select_a_date))

        val materialDatePicker = materialDateBuilder.build()

        binding.dateRangeLayout.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            binding.dateRangeLayout.isEnabled = false
        }

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            binding.dateRangeLayout.isEnabled = true
            val sDate = Date(selection.first!!)
            val eDate = Date(selection.second!!)
            if (sDate.compareTo(eDate) == 0) {
                binding.tvDateRange.text = simpleFormat.format(sDate)
            } else {
                binding.tvDateRange.text = getString(
                    R.string.date_range_,
                    simpleFormat.format(sDate),
                    simpleFormat.format(eDate)
                )
            }
            getAllOrders(
                Api.get_saved_order + "?user_id=" + sharePrefUtils.getString(Api.USER_ID) +"&start_date=" + df.format(
                    sDate
                ) + " 00:00:00" + "&end_date=" + df.format(eDate) + " 23:59:59" + "&order_status=PENDING, DELIVERED"
            )

        }

        materialDatePicker.addOnNegativeButtonClickListener {
            binding.dateRangeLayout.isEnabled = true
        }

        getAllOrders(Api.get_saved_order + "?user_id=" + sharePrefUtils.getString(Api.USER_ID) +/*"&route_id="+route_id+*/"&start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59" + "&order_status=PENDING, DELIVERED")
    }

    private fun getAllOrders(url: String) {
        binding.progressBar.visibility = View.GONE
        binding.progressBarOrder.visibility = View.VISIBLE
        ApiServices.apiGET(
            url,
            queue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        binding.progressBar.visibility = View.GONE
                        binding.progressBarOrder.visibility = View.GONE
                        val itemList: ArrayList<OrderList> = ArrayList()

                        val obj = JSONObject(response)
                        val orderArray = obj.getJSONArray("orders")
                        if (orderArray.length() > 0) {
                            for (i in 0 until orderArray.length()) {
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
                                                        brandObj.getDouble("delivered_amount"),
                                                        if (brandObj.has("offer_id")) brandObj.getString(
                                                            "offer_id"
                                                        ) else "",
                                                        if (orderObj.has("offers")) Gson().fromJson< List<Offer>>(orderObj.getString("offers")) else emptyList()
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
                                                        brandObj.getDouble("ordered_amount"),
                                                        if (brandObj.has("offer_id")) brandObj.getString(
                                                            "offer_id"
                                                        ) else "",
                                                        if (orderObj.has("offers")) Gson().fromJson< List<Offer>>(orderObj.getString("offers")) else emptyList()
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
                        adapter.updateList(itemList)
                    } catch (e: Exception) {
                        binding.progressBarOrder.visibility = View.GONE
                        e.printStackTrace()
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    binding.progressBarOrder.visibility = View.GONE
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    binding.progressBarOrder.visibility = View.GONE
                }

            })
    }

    override fun onEdit(order: OrderList) {}

}