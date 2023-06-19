package com.barikoi.cnlapp.TradeOffers

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.TradeOffers.Adapter.TradeOfferListAdapter
import com.barikoi.cnlapp.TradeOffers.Model.ProductAll
import com.barikoi.cnlapp.TradeOffers.Model.TradeProduct
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils

import kotlinx.android.synthetic.main.activity_trade_offers.*
import org.json.JSONObject
import kotlin.collections.ArrayList

class TradeOffersActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    val itemList: ArrayList<ProductAll> = ArrayList()
    val filterList: ArrayList<ProductAll> = ArrayList()
    private var adapter: TradeOfferListAdapter? = null
    var territoryId: String ? = ""
    var srCode: String ? = ""
    var userId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_offers)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()

        territoryId = prefs!!.getString(Api.TERRITORY_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        srCode = prefs!!.getString(Api.SR_CODE, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO")) {
            getProducts(Api.all_product_list + "?with_stock=1&territory_id="+territoryId)
        }else{
            getProducts(Api.all_product_list + "?with_stock=1&user_id="+userId/*+"&route_id="+routeId*/)
        }
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        filterLayout.setOnClickListener {
            val popup = PopupMenu(applicationContext, filterTitle)
            popup.menuInflater.inflate(R.menu.filter_menu_trade, popup.menu)
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.menu_all_product->{
                            if (itemList.size > 0){
                                if (adapter != null) {
                                    adapter = TradeOfferListAdapter(itemList)
                                    productList2.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                                    productList2.adapter = adapter
                                    adapter!!.notifyDataSetChanged()
                                }

                            }
                            tvTitle.setText(resources.getString(R.string.product_list))
                        }
                        R.id.menu_trade_offers->{
                            filterList.clear()
                            filterList.addAll(itemList)
                            filterList.removeIf {
                                it.tradeList.size == 0
                            }
                            adapter = TradeOfferListAdapter(filterList)
                            productList2.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                            productList2.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                            tvTitle.setText(resources.getString(R.string.title_trade_offers))
                        }
                    }
                    return true
                }

            })
            popup.show()
        }

    }

    private fun getProducts(url: String) {

        ApiServices.apiGET(url, queue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        itemList.clear()
                        progressBar.visibility = View.GONE
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")
                        if (productsArray.length() > 0){
                            for (i in 0 until productsArray.length()){
                                val tradeProducts: ArrayList<TradeProduct> = ArrayList()
                                val productObj = productsArray.getJSONObject(i)
                                /*if (productObj.has("trade") && !productObj.isNull("trade")){
                                    if (productObj.getJSONArray("trade").length()>0){
                                        tradeProducts.add(
                                            TradeProduct(
                                                productObj.getJSONArray("trade").getJSONObject(0).getString("start_date"),
                                                productObj.getJSONArray("trade").getJSONObject(0).getString("end_date"),
                                                productObj.getJSONArray("trade").getJSONObject(0).getString("discounted_price")
                                            )
                                        )
                                    }
                                }*/
                                var imageUrl = "null"
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
                                    ProductAll(
                                        productObj.getString("id"),
                                        productObj.getString("product_name"),
                                        productObj.getString("unit_name"),
                                        productObj.getString("current_available_stock"),
                                        productObj.getString("discounted_unit_price"),
                                        imageUrl,
                                        tradeProducts)
                                )
                            }
                        }


                        adapter = TradeOfferListAdapter(itemList)
                        productList2.adapter = adapter
                        adapter!!.notifyDataSetChanged()


                    }
                }catch (e: Exception){
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
                ViewUtils.getErrorResponse(error, applicationContext)
                progressBar.visibility = View.GONE
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }

        })
    }

}