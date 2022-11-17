package com.barikoi.cnlapp.ProductStock

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.activity_product_stock_update.*
import kotlinx.android.synthetic.main.activity_product_stock_update.btnBack
import kotlinx.android.synthetic.main.activity_product_stock_update.productList
import kotlinx.android.synthetic.main.activity_trade_offers.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ProductStockUpdateActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_stock_update)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        setDateFilter()
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        //val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        getProductStock(Api.all_product_list+"?start_date="+StartDate+"&end_date="+EndDate+"&with_stock=1&with_order=1")
    }

    private fun getProductStock(url: String) {

        ApiServices.apiGET(url, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val itemList: ArrayList<ProductStock> = ArrayList()
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")
                        if (productsArray.length() > 0){
                            for (i in 0 until productsArray.length()){
                                val productObj = productsArray.getJSONObject(i)
                                itemList.add(
                                    ProductStock(
                                        productObj.getString("id"),
                                        productObj.getString("product_name"),
                                        productObj.getString("image"),
                                        productObj.getString("productive_outlets")+" "+resources.getString(R.string.shops_ordered_this_month),
                                        productObj.getString("quantity"),
                                        productObj.getString("unit_name")
                                    )
                                )
                            }
                        }


                        val adapter = ProductStockAdapter(itemList)
                        productList.adapter = adapter
                        adapter!!.notifyDataSetChanged()


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
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

}