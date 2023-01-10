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
    var territoryId: String ? = ""
    var userId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_stock_update)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        territoryId = prefs!!.getString(Api.TERRITORY_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

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

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO")) {
            getProductStock(Api.all_product_list + "?start_date=" + EndDate +" 00:00:00"+ "&end_date=" + EndDate +" 23:59:59"+ "&with_stock=1&with_order=1&territory_id="+territoryId)
        }else{
            getProductStock(Api.all_product_list + "?start_date=" + StartDate  +" 00:00:00"+ "&end_date=" + EndDate +" 23:59:59"+ "&with_stock=1&with_order=1&user_id="+userId/*+"&route_id="+routeId*/)
        }
    }

    private fun getProductStock(url: String) {
        ApiServices.apiGET(url, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val itemList: ArrayList<ProductStock> = ArrayList()
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")
                        var imageUrl = "null"
                        if (productsArray.length() > 0){
                            for (i in 0 until productsArray.length()){
                                val productObj = productsArray.getJSONObject(i)
                                if (productObj.has("images") && !productObj.isNull("images")){
                                    val imageArray = productObj.getJSONArray("images")
                                    if (imageArray.length() > 0){
                                        val imageobj = imageArray.getJSONObject(0)
                                        if (imageobj.has("image_url")){
                                            imageUrl = imageobj.getString("image_url")
                                        }
                                    }
                                }
                                itemList.add(
                                    ProductStock(
                                        productObj.getString("id"),
                                        productObj.getString("product_name"),
                                        imageUrl,
                                        productObj.getString("productive_outlets")+" "+resources.getString(R.string.shops_ordered_this_month),
                                        productObj.getString("current_available_stock"),
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