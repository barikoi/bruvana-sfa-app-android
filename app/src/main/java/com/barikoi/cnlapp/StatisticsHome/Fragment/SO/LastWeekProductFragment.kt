package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_last_week_product.progressBar
import kotlinx.android.synthetic.main.fragment_last_week_product.tabLayout
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class LastWeekProductFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_week_product, container, false)
    }

    private fun init() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val StartDate = df.format(start)
        val EndDate = df.format(end)
        getSummaryProducts(Api.all_product_list + "?user_id=" + userId + "&route_id=" + routeId + "&with_last_week_order=1")
    }


    private fun getSummaryProducts(url: String) {
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, "", object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    progressBar.visibility = View.GONE
                    if (response != null) {
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        val obj = JSONObject(response)
                        val productsArray = obj.getJSONArray("products")
                        if (productsArray.length() > 0) {
                            for (i in 0 until productsArray.length()) {
                                val productObj = productsArray.getJSONObject(i)
                                if (!productObj.isNull("delivered_amount")) {
                                    if (productObj.getDouble("delivered_amount") > 0.0) {
                                        val productName = productObj.getString("product_name")
                                        val totalPrice = dformat.format(productObj.getString("delivered_amount").toDouble())
                                        itemList.add(Pair(productName, totalPrice))
                                    }
                                }

                            }
                        }
                        createTable(itemList)


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

    private fun createTable(data: ArrayList<Pair<String, String>>) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        if (data.size >0) {
            for (i in 0 until data.size) {
                val tr = TableRow(mContext)
                val c1 = TextView(mContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.setText(data.get(i).first)
                val c2 = TextView(mContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.setText(data.get(i).second)
                tr.addView(c1)
                tr.addView(c2)
                tabLayout.addView(tr)
            }
        }else{
            val valueTV = TextView(mContext)
            valueTV.text = "No Products on the list"
            valueTV.textSize = 20f
            valueTV.gravity = Gravity.CENTER
            valueTV.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)

            tabLayout.addView(valueTV)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }
}