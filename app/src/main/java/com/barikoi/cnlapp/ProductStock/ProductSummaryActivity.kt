package com.barikoi.cnlapp.ProductStock

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_product_summary.*
import kotlinx.android.synthetic.main.activity_product_summary.btnBack
import kotlinx.android.synthetic.main.activity_product_summary.dateRangeLayout
import kotlinx.android.synthetic.main.activity_product_summary.productList
import kotlinx.android.synthetic.main.activity_product_summary.spinnerLayout
import kotlinx.android.synthetic.main.activity_product_summary.tvDateRange
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ProductSummaryActivity : AppCompatActivity() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    var token : String? = null
    val dhList: ArrayList<Pair<String, String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_summary)

        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")

        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)){
            spinnerLayout.visibility = View.VISIBLE
            getDHList()
            spinnerDistributorHouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (spinnerDistributorHouse.adapter.count >0) {
                        /*selected_so = p2
                        if (p2>0) {
                            selected_so_id = soList[p2 - 1].id
                        }
                        if (p2 == 0) {
                            setDateFilter("&with_to=1")
                        } else {
                            setDateFilter("")
                        }*/
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

            }
        }else{
            spinnerLayout.visibility = View.GONE
            setDateFilter()
        }
    }


    private fun setDateFilter() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                /*editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()*/
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                /*editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()*/
            }
            getProductStock(Api.all_product_list+"?start_date="+df.format(s_date)+"&end_date="+df.format(e_date)+"&with_stock=1&with_order=1")

        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }

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
                                        resources.getString(R.string.sold_in)+" "+productObj.getString("productive_routes")+" "+resources.getString(R.string.route),
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

    private fun getDHList() {
        ApiServices.apiGET(
            Api.get_dh_list,
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
                val dhArray = obj.getJSONArray("distributor_houses")
                val dhNameList: ArrayList<String> = ArrayList()
                if (dhArray.length() >0){
                    for (i in 0 until dhArray.length()) {
                        val dhObj = dhArray.getJSONObject(i)
                        dhList.add(
                            Pair(dhObj.getString("dh_name"), dhObj.getString("dh_code"))
                        )
                        dhNameList.add(dhObj.getString("dh_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_spinner_item, dhList
                )
                spinnerDistributorHouse.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}