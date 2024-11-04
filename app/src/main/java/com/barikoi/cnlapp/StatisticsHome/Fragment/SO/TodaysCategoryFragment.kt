package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Model.Categories
import com.barikoi.cnlapp.databinding.FragmentLastWeekCategoryBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class TodaysCategoryFragment : Fragment() {
    private lateinit var binding: FragmentLastWeekCategoryBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = null
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLastWeekCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_WEEK, -7)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val StartDate = df.format(start)
        val EndDate = df.format(end)
        getSummaryCategory(Api.get_last_week_category + "?user_id=" + userId + "&route_id=" + routeId + "&today_category=1")
    }

    private fun getSummaryCategory(url: String) {
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    binding.progressBar.visibility = View.GONE
                    if (response != null) {
                        var dformat = DecimalFormat("#.##")
                        val itemList: ArrayList<Categories> = ArrayList()
                        val obj = JSONObject(response)
                        val categoryArray = obj.getJSONArray("outlet_categories")
                        if (categoryArray.length() > 0) {
                            itemList.add(
                                Categories(
                                    getString(R.string.category),
                                    getString(R.string.total_outlet),
                                    getString(R.string.order_done),
                                    getString(R.string.order_value)
                                )
                            )
                            for (i in 0 until categoryArray.length()) {
                                val productObj = categoryArray.getJSONObject(i)
                                if (!productObj.getString("outlet_category")
                                        .equals("") && !productObj.getString("outlet_category")
                                        .equals("null")
                                ) {
                                    val outletCatName = productObj.getString("outlet_category")
                                    val outletCount = productObj.getString("total_outlet")
                                    val orderDone = productObj.getString("outlet_count_ordered")
                                    val orderValue = dformat.format(
                                        productObj.getString("order_value").toDouble()
                                    )
                                    val sumOutletCount = productObj.getString("sum_total_outlet")
                                    val sumOrderDone =
                                        productObj.getString("sum_outlet_count_ordered")
                                    val sumOrderValue = dformat.format(
                                        productObj.getString("sum_order_value").toDouble()
                                    )

                                    if (i == categoryArray.length() - 1) {
                                        itemList.add(
                                            Categories(
                                                outletCatName,
                                                outletCount,
                                                orderDone,
                                                orderValue
                                            )
                                        )
                                        itemList.add(
                                            Categories(
                                                getString(R.string.total_order),
                                                sumOutletCount,
                                                sumOrderDone,
                                                sumOrderValue
                                            )
                                        )
                                    } else {
                                        itemList.add(
                                            Categories(
                                                outletCatName,
                                                outletCount,
                                                orderDone,
                                                orderValue
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        createTable(itemList)


                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.progressBar.visibility = View.GONE
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
                binding.progressBar.visibility = View.GONE
            }

            override fun onException(e: Exception) {
                binding.progressBar.visibility = View.GONE
            }

        })

    }

    private fun createTable(data: ArrayList<Categories>) {
        AppLogger.log("DATA:: ${data.get(0)}")
        binding.tabLayout.isStretchAllColumns = true
        binding.tabLayout.bringToFront()
        val colorsTxt: Array<String> = mContext!!.resources.getStringArray(R.array.colors)
        for (i in 0 until data.size) {
            val tr = TableRow(mContext)
            val trborder = TableRow(mContext)
            trborder.setBackgroundColor(resources.getColor(R.color.colorAccent))
            val image = ImageView(mContext)
            image.setImageDrawable(resources.getDrawable(R.drawable.ic_dot))
            image.drawable.setTint(Color.parseColor(colorsTxt[i]))
            val c1 = TextView(mContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).outlet_category)
            val c2 = TextView(mContext)
            c2.gravity = Gravity.CENTER
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).total_outlet)
            val c3 = TextView(mContext)
            c3.gravity = Gravity.CENTER
            c3.setTextColor(resources.getColor(R.color.text_title))
            c3.setText(data.get(i).order_done)
            val c4 = TextView(mContext)
            c4.gravity = Gravity.CENTER
            c4.setTextColor(resources.getColor(R.color.text_title))
            c4.setText(data.get(i).order_value)
            tr.addView(image)
            tr.addView(c1)
            tr.addView(c2)
            tr.addView(c3)
            tr.addView(c4)
            binding.tabLayout.addView(tr)
            if (i == 0 || i == data.size - 1) {
                image.visibility = View.INVISIBLE
            }
        }
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