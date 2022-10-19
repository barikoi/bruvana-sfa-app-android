package com.barikoi.cnlapp.Fragment.so_view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ShopListAdapter
import com.barikoi.cnlapp.Adapter.so_view.ProductListAdapter
import com.barikoi.cnlapp.Adapter.so_view.ShopSelectAdapter
import com.barikoi.cnlapp.Model.Products
import com.barikoi.cnlapp.Model.Shops
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.callback.OnValueChangeListener
import io.sentry.Sentry
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat


class ProductSelectFragment : Fragment(), OnValueChangeListener {

    var recylerView: RecyclerView? = null
    var shopTitle: TextView? = null
    var totalItemCount: TextView? = null
    var tvgrandTotal: TextView? = null
    var saveOrder: TextView? = null
    var mContext: Context? = null
    var queue: RequestQueue? = null
    var user_id : String? = null
    var shopName : String? =  null
    var listener: OnValueChangeListener? = null
    var et_search: AutoCompleteTextView? = null
    private var adapter: ProductListAdapter? = null
    var productsList: ArrayList<Products>? = ArrayList()
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var loading: ProgressBar? = null
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments

        if (bundle != null) {
            shopName = bundle.getString(Api.SELECTED_SHOP)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product_select, container, false)

        shopTitle = view.findViewById(R.id.selectedShop)
        et_search = view.findViewById(R.id.editTextSearchProduct)
        recylerView = view.findViewById(R.id.productlist)
        totalItemCount = view.findViewById(R.id.totalItemCount)
        tvgrandTotal = view.findViewById(R.id.totalAmount)
        saveOrder = view.findViewById(R.id.saveOrder)
        loading = view.findViewById(R.id.progressBar)

        adapter = ProductListAdapter(ArrayList<Products>(), listener!!)

        shopTitle!!.text = shopName

        getAllProducts()

        et_search!!.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(s)
                if (s!!.length == 0){
                    getAllProducts()
                }
            }
            override fun afterTextChanged(p0: Editable?) {

            }

        })

        saveOrder!!.setOnClickListener {

        }

        return view
    }

    private fun getAllProducts() {
        loading!!.visibility = View.VISIBLE
        val request = StringRequest(
            Request.Method.GET, Api.all_product_list+"?with_quantity_last_month=1&with_available_stock=1",
            {
                    response ->
                try {
                    loading!!.visibility = View.GONE

                    val data = JSONObject(response)
                    if (data.has("products") && !data.isNull("products")){
                        val productArray = data.getJSONArray("products")
                        if (productArray.length() > 0){
                            productsList!!.clear()
                            for (i in 0 until productArray.length()) {
                                val productObj = productArray.getJSONObject(i)

                                val productName = if (!productObj.isNull("product_name")) productObj.getString("product_name") else ""
                                val productCode = if (!productObj.isNull("product_code")) productObj.getString("product_code") else ""
                                val brandId = if (!productObj.isNull("brand_id")) productObj.getString("brand_id") else ""
                                val brandName = if (!productObj.isNull("brand__name")) productObj.getString("brand__name") else ""
                                //val price = if (!productObj.isNull("price")) productObj.getDouble("price") else 0.0
                                val discount = if (!productObj.isNull("discount")) productObj.getDouble("discount") else 0.0
                                val image = if (!productObj.isNull("image")) productObj.getString("image") else ""
                                val unitName = if (!productObj.isNull("unit_name")) productObj.getString("unit_name") else ""
                                val categoryName = if (!productObj.isNull("category_name")) productObj.getString("category_name") else ""
                                val qtyLastMonth = if (!productObj.isNull("quantity_last_month")) productObj.getInt("quantity_last_month") else 0
                                val availableStock = if (!productObj.isNull("current_available_stock")) productObj.getInt("current_available_stock") else 0
                                var price = 0.0
                                if (!productObj.isNull("price")) {
                                    price = productObj.getDouble("price")
                                } else {
                                    price = 0.0
                                }
                                val products = Products(
                                    productObj.getString("id"),
                                    productName, productCode, brandId, brandName, price, discount, image, unitName, categoryName,
                                    qtyLastMonth, availableStock)

                                productsList!!.add(products)
                            }

                            if (productsList!!.size > 0){
                                adapter = ProductListAdapter(productsList!!, listener!!)
                                recylerView!!.adapter = adapter
                                adapter!!.notifyDataSetChanged()
                            }
                        }

                    }
                }catch (e:Exception){
                    Sentry.captureException(e)
                    e.printStackTrace()
                }
            },
            { error ->
                loading!!.visibility = View.GONE
                if (error is TimeoutError) {
                    //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
                    Toast.makeText(mContext, "Request timeout!! Check your internet connection or Contact Admin", Toast.LENGTH_LONG).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(mContext, "Turn on your internet connection and Try again", Toast.LENGTH_LONG).show()
                }
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(mContext, data.getString("message"), Toast.LENGTH_LONG).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            })
        queue!!.add(request)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        queue = RequestQueueSingleton.getInstance(context).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        //token = prefs.getString("token", "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        mContext = context
        listener = this
    }

    override fun onValueChanged() {
        var dformat = DecimalFormat("#.##")
        var itemCount = 0
        var grandTotal = 0.0
        try {
            for (i in 0 until recylerView!!.adapter!!.itemCount) {
                val viewItem: View = recylerView!!.getChildAt(i)
                val etCount = viewItem.findViewById<View>(R.id.tvCount) as EditText
                itemCount = itemCount + etCount.text.toString().toInt()

                val tvGrandTotal = viewItem.findViewById<TextView>(R.id.tvTotalPrice) as TextView
                grandTotal = grandTotal + tvGrandTotal.text.toString().toDouble()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }


        totalItemCount!!.setText(itemCount.toString()+"Items")
        tvgrandTotal!!.setText("Total "+dformat.format(grandTotal).toString())

    }


}