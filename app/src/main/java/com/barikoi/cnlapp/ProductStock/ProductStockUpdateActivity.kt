package com.barikoi.cnlapp.ProductStock

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_product_stock_update.*
import kotlinx.android.synthetic.main.activity_product_stock_update.btnBack
import kotlinx.android.synthetic.main.activity_product_stock_update.productList
import kotlinx.android.synthetic.main.activity_product_stock_update.progressBar
import kotlinx.android.synthetic.main.activity_product_stock_update.spinnerDistributorHouse
import kotlinx.android.synthetic.main.activity_product_stock_update.spinnerLayoutRoute
import kotlinx.android.synthetic.main.activity_product_summary.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ProductStockUpdateActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var territoryId: String ? = ""
    var token : String? = ""
    var userId: String ? = ""
    var routeId: String ? = ""
    var territorySuffix : String? = ""
    var selectedTerritoryId : String? = null
    val dhList: ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_stock_update)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        token = prefs!!.getString(Api.TOKEN, "")
        editor = prefs!!.edit()
        territoryId = prefs!!.getString(Api.TERRITORY_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)){
            spinnerLayoutRoute.visibility = View.VISIBLE
            getDHList()
            spinnerDistributorHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (spinnerDistributorHouse.adapter.count >0) {
                        selectedTerritoryId = dhList.get(p2).second
                        territorySuffix = "&db_house_id="+selectedTerritoryId
                        setDateFilter()
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }

        }else{
            territorySuffix = "&user_id="+userId/*+"&route_id="+routeId*/
            spinnerLayoutRoute.visibility = View.GONE
            setDateFilter()
        }
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        //val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        getProductStock(Api.all_product_list + "?start_date=" + EndDate +" 00:00:00"+ "&end_date=" + EndDate +" 23:59:59"+ "&with_stock=1&with_order=1"+territorySuffix)
    }

    private fun getProductStock(url: String) {
        progressBar.visibility = View.VISIBLE
        productList.visibility = View.GONE
        ApiServices.apiGET(url, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        progressBar.visibility = View.GONE
                        productList.visibility = View.VISIBLE
                        val itemList: ArrayList<ProductStock> = ArrayList()
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")

                        if (productsArray.length() > 0){
                            for (i in 0 until productsArray.length()){
                                var imageUrl = "null"
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
                    progressBar.visibility = View.GONE
                    productList.visibility = View.GONE
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
                progressBar.visibility = View.GONE
                productList.visibility = View.GONE
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                productList.visibility = View.GONE
            }

        })
    }

    private fun getDHList() {
        ApiServices.apiGET(
            Api.get_dh_list+"?territory_id="+territoryId,
            queue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewDHList(response)
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

    private fun viewDHList(response: String) {
        try {
            if (response != null){
                dhList.clear()
                val obj = JSONObject(response)
                val dhArray = obj.getJSONArray("db_houses")
                val dhNameList: ArrayList<String> = ArrayList()
                if (dhArray.length() >0){
                    for (i in 0 until dhArray.length()) {
                        val dhObj = dhArray.getJSONObject(i)
                        dhList.add(
                            Pair(dhObj.getString("db_house_name"), dhObj.getString("id"))
                        )
                        dhNameList.add(dhObj.getString("db_house_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_spinner_item, dhNameList
                )
                spinnerDistributorHouse.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

}