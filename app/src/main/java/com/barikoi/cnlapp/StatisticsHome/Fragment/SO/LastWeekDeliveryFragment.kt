package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_last_week_delivery.*
import org.json.JSONObject
import kotlin.collections.ArrayList


class LastWeekDeliveryFragment : Fragment() {

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token : String? = null
    var srId: String ? = ""
    var userId: String ? = ""
    var routeId: String ? = ""

    var itemList : ArrayList<OutletStatistics> = ArrayList()

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
        getLastDeliveryItems(Api.get_last_week_delivery_bounce+"?user_id="+userId+"&route_id="+routeId+"&with_delivered=1&with_last_week_order=1")
    }

    private fun getLastDeliveryItems(url: String) {

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        itemList.clear()
                        val obj = JSONObject(response)
                        val outletssArray = obj.getJSONArray("outlets")
                        if (outletssArray.length() > 0){
                            for (i in 0 until outletssArray.length()){
                                val productList: ArrayList<ProductStatistics> = ArrayList()
                                val outletObj = outletssArray.getJSONObject(i)
                                val brandArray = outletObj.getJSONArray("products")
                                if (brandArray.length() >0){
                                    for (j in 0 until brandArray.length()){
                                        val brandObj = brandArray.getJSONObject(j)
                                        if (brandObj.getInt("quantity") > 0) {
                                            productList.add(
                                                ProductStatistics(
                                                    brandObj.getString("product_id"),
                                                    brandObj.getString("product"),
                                                    brandObj.getString("unit_name"),
                                                    /*brandObj.getString("brand_id"),*/
                                                    brandObj.getDouble("unit_price"),
                                                    brandObj.getDouble("total_price"),
                                                    brandObj.getInt("quantity"),
                                                    brandObj.getInt("quantity"),
                                                    brandObj.getInt("bounce"),
                                                    brandObj.getDouble("total_price")
                                                )
                                            )
                                        }
                                    }
                                }
                                if (productList.size > 0) {
                                    itemList.add(
                                        OutletStatistics(
                                            outletObj.getString("outlet_id"),
                                            outletObj.getString("outlet_name"),
                                            outletObj.getString("outlet_code"),
                                            outletObj.getString("outlet_category"),
                                            outletObj.getString("ordered_at"),
                                            productList
                                        )
                                    )
                                }
                            }
                        }

                        val adapter = OutletAdapter(itemList, "delivery")
                        listView.adapter = adapter
                        adapter.notifyDataSetChanged()


                    }
                }catch (e: Exception){
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
                ViewUtils.getErrorResponse(error, mContext!!)
            }

            override fun onException(e: Exception) {
                TODO("Not yet implemented")
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