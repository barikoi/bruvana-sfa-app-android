package com.barikoi.cnlapp.Order_Delivery.Fragments

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Order_Delivery.Adapter.OrderDeliveryListAdapter
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.EndDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.StartDate
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity.Companion.etSearchShop
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.RoomDb.AppDatabase
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletProductAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.databinding.FragmentDeliveredOrderBinding
import com.barikoi.cnlapp.order_create.Callback.DialogListener
import com.barikoi.cnlapp.order_create.Callback.OnEditOrderListener
import com.barikoi.cnlapp.order_create.Callback.OrderListSuccessListener
import com.barikoi.cnlapp.order_create.RoomDB.OrderList
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.Constants
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class DeliveredOrderFragment : Fragment(), OrderListSuccessListener, OnEditOrderListener {
    private lateinit var binding: FragmentDeliveredOrderBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkforOrders(
            queue!!,
            token!!,
            user_id!!,
            sr_id!!,
            territory_id!!,
            sharePrefUtils.getString(Constants.REGION_ID)!!,
            StartDate!!,
            EndDate!!,
            sharePrefUtils
        )

        etSearchShop!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapterDelivery.filter.filter(s)
                if (s!!.isEmpty()) {
                    if (sr_id!!.isEmpty()) {
                        getAllOrders(
                            Api.get_saved_order + "?user_id=" + user_id + "&start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&territory_id=" + territory_id + "&order_status=DELIVERED&include_filter_by_user_id=1",
                            queue!!,
                            token!!,
                            mCallback2!!
                        )
                    } else {
                        getAllOrders(
                            Api.get_saved_order + "?user_id=" + user_id + "&start_date=" + StartDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&order_status=DELIVERED",
                            queue!!,
                            token!!,
                            mCallback2!!
                        )
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeliveredOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        var mCallback2: OrderListSuccessListener? = DeliveredOrderFragment()
        var token: String? = null
        var user_id: String? = null
        var user_type: String? = null
        var sr_id: String? = null
        var territory_id: String? = null
        var route_id: String? = null
        private var prefs: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null
        var queue: RequestQueue? = null
        var appDatabase: AppDatabase? = null
        private var listener: OnEditOrderListener? = null
        val orderList: ArrayList<OrderList> = ArrayList()
        lateinit var adapterDelivery: OrderDeliveryListAdapter

        fun checkforOrders(
            queue: RequestQueue,
            token: String,
            user_id: String,
            sr_id: String,
            territory_id: String,
            regionId: String,
            start: String,
            end: String,
            sharePrefUtils: SharePrefUtils
        ) {
            if (mCallback2 != null) {
                if (sr_id.isEmpty()) {
                    if (sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")) {
                        getAllOrders(
                            Api.get_saved_order + "?user_id=" + user_id + "&start_date=" + start + " 00:00:00" + "&end_date=" + end + " 23:59:59" + "&territory_id=" + territory_id + "&order_status=DELIVERED&region_id=${regionId}&include_filter_by_asm=1",
                            queue,
                            token,
                            mCallback2!!
                        )
                    } else {
                        getAllOrders(
                            Api.get_saved_order + "?user_id=" + user_id + "&start_date=" + start + " 00:00:00" + "&end_date=" + end + " 23:59:59" + "&territory_id=" + territory_id + "&order_status=DELIVERED&include_filter_by_user_id=1",
                            queue,
                            token,
                            mCallback2!!
                        )
                    }
                } else {
                    getAllOrders(
                        Api.get_saved_order + "?user_id=" + user_id +/*"&route_id="+route_id+*/"&start_date=" + start + " 00:00:00" + "&end_date=" + end + " 23:59:59" + "&order_status=DELIVERED",
                        queue,
                        token,
                        mCallback2!!
                    )
                }

            }

        }

        fun getAllOrders(
            url: String,
            queue: RequestQueue,
            token: String,
            callback: OrderListSuccessListener
        ) {
            ApiServices.apiGET(url, queue, token, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    AppLogger.log("getAllOrders onResponseSuccess: $response")
                    try {

                        val obj = JSONObject(response)
                        val orderArray = obj.getJSONArray("orders")
                        callback.onSuccess(orderArray)

                    } catch (e: Exception) {
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
                    callback.onFailure(error)
                }

                override fun onException(e: Exception) {
                }

            })
        }
    }

    override fun onSuccess(array: JSONArray) {
        binding.progressBar2.visibility = View.GONE
        orderList.clear()
        try {
            if (array.length() > 0) {
                /*no_route_check.visibility = View.GONE
                bodyLayout.visibility = View.VISIBLE*/
                for (i in 0 until array.length()) {
                    val orderObj = array.getJSONObject(i)
                    if (orderObj.getString("order_status").equals("DELIVERED", true)) {
                        val brandArray = orderObj.getJSONArray("products")
                        //tvRouteName.setText(orderObj.getString("route_name"))
                        val productItems: ArrayList<Products> = ArrayList()
                        if (brandArray.length() > 0) {
                            for (j in 0 until brandArray.length()) {
                                val brandObj = brandArray.getJSONObject(j)
                                var bounce = 0
                                if (brandObj.has("bounced_quantity")) {
                                    bounce = brandObj.getInt("bounced_quantity")
                                }
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
                                            bounce,
                                            brandObj.getInt("delivered_quantity"),
                                            brandObj.getDouble("delivered_amount")
                                        )
                                    )
                                }
                            }
                        }
                        orderList.add(
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
                    }
                }
            } else {
                binding.noRouteCheck.visibility = View.VISIBLE
                binding.bodyLayout.visibility = View.GONE
                binding.btnTryAgain.setOnClickListener {
                    /*checkforOrders(
                        queue!!,
                        token!!,
                        sr_id!!,
                        route_id!!
                    )*/
                }

            }
            orderList.sortByDescending {
                it.orderId
            }

            binding.orderListView.apply {
                adapterDelivery = OrderDeliveryListAdapter(
                    orderList,
                    listener!!,
                    sharePrefUtils.getString(Api.USER_TYPE)!!
                )
                binding.orderListView.adapter = adapterDelivery
                adapterDelivery.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ///Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onFailure(error: VolleyError) {
        binding.progressBar2.visibility = View.GONE
        ViewUtils.getErrorResponse(error, requireContext())
    }

    override fun onDataSet(StartDate: Date, EndDate: Date) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")

        user_type = prefs!!.getString(Api.USER_TYPE, "")
        if (user_type.equals("TO", true)) {
            sr_id = ""
            route_id = ""
            user_id = OrderDeliveryUpdateActivity.user_id
        } else if (user_type.equals("ASM", true)) {
            sr_id = ""
            route_id = ""
            user_id = OrderDeliveryUpdateActivity.user_id
        } else {
            sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
            route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
            user_id = prefs!!.getString(Api.USER_ID, "")
        }
        territory_id = prefs!!.getString(Api.TERRITORY_ID, "")
        appDatabase = AppDatabase.getInstance(context)
        listener = this
        mCallback2 = this

    }

    override fun onEdit(order: OrderList) {

        //viewDialog(mContext!!, order)
    }

    fun viewDialog(mContext: Context, order: OrderList) {
        val dialog = Dialog(mContext)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup_order_status_update)
        val btnClose = dialog.findViewById<ImageButton>(R.id.btnClose)
        val btnSubmit = dialog.findViewById<AppCompatButton>(R.id.btnSubmit)
        val outletName = dialog.findViewById<TextView>(R.id.outletName)
        val listView = dialog.findViewById<RecyclerView>(R.id.productList)
        val tvLastOrderDate = dialog.findViewById<TextView>(R.id.lastOrderDate)
        val tvItemCount = dialog.findViewById<TextView>(R.id.itemCount)
        val tvGrandTotal = dialog.findViewById<TextView>(R.id.grandTotal)
        val statusGroup = dialog.findViewById<RadioGroup>(R.id.status_group)
        val radio_group: RadioGroup? = RadioGroup(mContext)
        val itemValue: ArrayList<String> = ArrayList()
        itemValue.add(mContext.resources.getString(R.string.pending))
        itemValue.add(mContext.resources.getString(R.string.delivered))
        itemValue.add(mContext.resources.getString(R.string.bounced))
        radio_group!!.setOrientation(RadioGroup.HORIZONTAL)
        for (i in itemValue.indices) {
            val rbn = RadioButton(mContext)
            rbn.setText(itemValue.get(i))
            rbn.id = i
            rbn.setTextColor(mContext.resources.getColor(R.color.text_title))
            rbn.buttonTintList =
                ColorStateList.valueOf(mContext.resources.getColor(R.color.cnl_color_1))
            radio_group.addView(rbn)
        }
        val checkedid = 1
        var isChecked = 0
        statusGroup.addView(radio_group)
        radio_group.check(checkedid)
        radio_group.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            isChecked = checkedId
        })

        val dformat = DecimalFormat("#.##")
        outletName.text = order.outletName
        val oldDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val df = SimpleDateFormat("dd LLL yyyy", Locale.ENGLISH)
        val orderDate = df.format(oldDate.parse(order.orderedAt))
        tvLastOrderDate.text = mContext.resources.getString(R.string.last_order_date) + orderDate
        var grandTotal = 0.0
        var itemCount = 0
        val brandsStatistics: ArrayList<ProductStatistics> = ArrayList()
        if (order.brands_array.size > 0) {
            brandsStatistics.clear()
            for (i in 0 until order.brands_array.size) {
                brandsStatistics.add(
                    ProductStatistics(
                        order.brands_array[i].productId,
                        order.brands_array[i].productName,
                        order.brands_array[i].productCode,
                        order.brands_array[i].skuCode,
                        order.brands_array[i].categoryCode,
                        order.brands_array[i].categoryName,
                        order.brands_array[i].categoryId,
                        order.brands_array[i].unitName,
                        order.brands_array[i].unitId,
                        order.brands_array[i].unitCode,
                        order.brands_array[i].unitPrice,
                        order.brands_array[i].discountedUnitPrice,
                        order.brands_array[i].orderedTotalPrice,
                        order.brands_array[i].orderedQuantity,
                        order.brands_array[i].orderedQuantity,
                        order.brands_array[i].bouncedQuantity,
                        order.brands_array[i].orderedTotalPrice
                    )
                )
                grandTotal = grandTotal + order.brands_array[i].orderedTotalPrice
                itemCount = itemCount + order.brands_array[i].orderedQuantity
            }
            val adapter = OutletProductAdapter(brandsStatistics)
            listView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        tvGrandTotal!!.setText(dformat.format(grandTotal).toString())
        tvItemCount!!.setText(itemCount.toString() + mContext.resources.getString(R.string.items))

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        btnSubmit.setOnClickListener {
            ViewUtils.viewDialog(
                mContext,
                mContext.resources.getString(R.string.update_order_dialog),
                SpannableStringBuilder(),
                object :
                    DialogListener {
                    override fun onConfirmed() {
                        val status = itemValue.get(isChecked).uppercase(Locale.ENGLISH)
                        if (order.orderStatus.equals(status, true)) {
                            Toast.makeText(mContext, "Order status not changed", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            createOrder(
                                order,
                                status,
                                tvGrandTotal.text.toString(),
                                brandsStatistics,
                                dialog
                            )
                        }

                    }

                    override fun onCanceled() {

                    }

                })
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    }

    private fun createOrder(
        order: OrderList,
        status: String,
        grandTotal: String,
        updatedProducts: ArrayList<ProductStatistics>,
        dialog: Dialog
    ) {
        if (updatedProducts.size > 0) {
            val obj1 = JSONObject()
            val ordersArray = JSONArray()
            val orderObj = JSONObject()
            orderObj.put("outlet_id", order.outletId)
            orderObj.put("order_no", order.orderId)
            orderObj.put("sr_id", sr_id)
            /*orderObj.put("distributor_office_code", order.distOfficeCode)*/
            orderObj.put("grand_total", grandTotal)
            orderObj.put("order_status", status)
            val brandsArray = JSONArray()
            for (i in 0 until updatedProducts.size) {
                val brandObj = JSONObject()
                if (updatedProducts[i].quantity > 0) {
                    brandObj.put("product_id", updatedProducts[i].product_id)
                    brandObj.put("product", updatedProducts[i].product_name)
                    brandObj.put("quantity", updatedProducts[i].quantity.toString())
                    brandObj.put("bounce", updatedProducts[i].bounced_quantity.toString())
                    brandObj.put(
                        "discounted_unit_price",
                        updatedProducts[i].discounted_unit_price.toString()
                    )
                    brandObj.put("total_price", updatedProducts[i].total_price.toString())
                    brandObj.put("unit_name", updatedProducts[i].unit_name)
                }
                brandsArray.put(brandObj)
            }
            orderObj.put("products", brandsArray)
            ordersArray.put(orderObj)
            obj1.put("orders", ordersArray)

            if (obj1.length() > 0) {
                Log.d("ConfirmOrder", "response: " + obj1)
                submitOrder(obj1, dialog)
            }
        }
    }

    private fun submitOrder(orderObj: JSONObject, dialog: Dialog) {
        ApiServices.apiJSONObjectPOST(
            Api.update_saved_order,
            queue!!,
            token!!,
            orderObj,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    try {
                        Log.d("ConfirmOrder", "response api: " + response)
                        dialog.dismiss()
                        appDatabase!!.orderListDao().deleteALL()
                        appDatabase!!.saveOrderDao().deleteALL()
                        val message = response.getString("message")
                        ViewUtils.viewDialogResponse(
                            requireContext(),
                            message,
                            object : DialogListener {
                                override fun onConfirmed() {
                                    checkforOrders(
                                        queue!!,
                                        token!!,
                                        user_id!!,
                                        sr_id!!,
                                        territory_id!!,
                                        sharePrefUtils.getString(Constants.REGION_ID)!!,
                                        StartDate!!,
                                        EndDate!!,
                                        sharePrefUtils
                                    )
                                }

                                override fun onCanceled() {
                                    TODO("Not yet implemented")
                                }

                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    TODO("Not yet implemented")
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                }

                override fun onException(e: Exception) {
                    TODO("Not yet implemented")
                }

            })
    }

}