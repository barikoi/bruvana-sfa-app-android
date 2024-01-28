package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.OutletAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.OutletStatistics
import com.barikoi.cnlapp.StatisticsHome.Model.ProductStatistics
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_last_week_delivery.deliveryLayout
import kotlinx.android.synthetic.main.fragment_last_week_delivery.listView
import kotlinx.android.synthetic.main.fragment_last_week_delivery.progressBar
import org.json.JSONObject


class LastWeekDeliveryFragment : Fragment() {

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = null
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""

    var itemList: ArrayList<OutletStatistics> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        getLastDeliveryItems(Api.verified_shop_list + "?user_id=" + userId + "&route_id=" + routeId + "&with_last_week_order=1")
    }

    private fun getLastDeliveryItems(url: String) {

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    progressBar.visibility = View.GONE
                    if (response != null) {
                        itemList.clear()
                        val obj = JSONObject(response)
                        val outletssArray = obj.getJSONArray("outlets")
                        if (outletssArray.length() > 0) {
                            for (i in 0 until outletssArray.length()) {
                                val productList: ArrayList<ProductStatistics> = ArrayList()
                                val outletObj = outletssArray.getJSONObject(i)
                                val ordersArray = outletObj.getJSONArray("orders")
                                var imageUrl = "null"
                                if (outletObj.has("images") && !outletObj.isNull("images")){
                                    val imageArray = outletObj.getJSONArray("images")
                                    if (imageArray.length() > 0){
                                        val imageobj = imageArray.getJSONObject(0)
                                        if (imageobj.has("image_url")){
                                            imageUrl = imageobj.getString("image_url")
                                        }
                                    }
                                }
                                if (ordersArray.length() > 0) {
                                    for (j in 0 until ordersArray.length()) {
                                        val orderObj = ordersArray.getJSONObject(j)
                                        if (orderObj.getString("order_status")
                                                .equals("DELIVERED", true)
                                        ) {
                                            val brandArray = orderObj.getJSONArray("products")
                                            if (brandArray.length() > 0) {
                                                for (k in 0 until brandArray.length()) {
                                                    val brandObj = brandArray.getJSONObject(k)
                                                    if (brandObj.getInt("ordered_quantity") > 0) {
                                                        productList.add(
                                                            ProductStatistics(
                                                                brandObj.getString("product_id"),
                                                                brandObj.getString("product_name"),
                                                                brandObj.getString("product_code"),
                                                                brandObj.getString("sku_code"),
                                                                brandObj.getString("category_code"),
                                                                brandObj.getString("category_name"),
                                                                brandObj.getString("category_id"),
                                                                brandObj.getString("unit_name"),
                                                                brandObj.getString("unit_id"),
                                                                brandObj.getString("unit_code"),
                                                                brandObj.getDouble("unit_price"),
                                                                brandObj.getDouble("discounted_unit_price"),
                                                                brandObj.getDouble("ordered_amount"),
                                                                brandObj.getInt("ordered_quantity"),
                                                                brandObj.getInt("delivered_quantity"),
                                                                brandObj.getInt("bounced_quantity"),
                                                                brandObj.getDouble("delivered_amount")
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                            if (productList.size > 0) {
                                                itemList.add(
                                                    OutletStatistics(
                                                        outletObj.getString("id"),
                                                        outletObj.getString("outlet_name"),
                                                        outletObj.getString("outlet_code"),
                                                        imageUrl,
                                                        outletObj.getString("outlet_category"),
                                                        orderObj.getString("ordered_at"),
                                                        productList
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (itemList.size > 0) {
                            listView.visibility = View.VISIBLE
                            val adapter = OutletAdapter(itemList, "delivery")
                            listView.adapter = adapter
                            adapter.notifyDataSetChanged()
                        }else{
                            val valueTV = TextView(mContext)
                            valueTV.text = "No Order on the list"
                            valueTV.textSize = 20f
                            valueTV.gravity = Gravity.CENTER
                            valueTV.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                            listView.visibility = View.GONE
                            deliveryLayout.addView(valueTV)
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBar.visibility = View.GONE
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, mContext!!)
                progressBar.visibility = View.GONE
            }

            override fun onException(e: Exception) {
                progressBar.visibility = View.GONE
            }

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_week_delivery, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }
}