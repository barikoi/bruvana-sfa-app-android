package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.bpcCount
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.lpcCount
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.ovCount
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.progressBarHome
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.summaryLayout
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.tabLayout2
import kotlinx.android.synthetic.main.fragment_last_week_summary_t_o.tryAgain
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class LastWeekSummaryTOFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String ? = ""
    var territoryId: String ? = ""
    var employeeId: String? = ""
    var userId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_week_summary_t_o, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLastWeekSummary()

        tryAgain.setOnClickListener {
            setLastWeekSummary()
        }
    }

    private fun setLastWeekSummary() {
        try{
            progressBarHome.visibility = View.VISIBLE
            val dformat = DecimalFormat("#.##")
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_WEEK, -7)
            val end = Calendar.getInstance().time
            val start = c.time
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val simpleFormat = SimpleDateFormat("LLL dd", Locale.getDefault())
            //tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
            val StartDate = df.format(start)
            val EndDate = df.format(start)
            ApiServices.apiGET(Api.get_all_so_list+"?last_week_summary=1&start_date="+StartDate+" 00:00:00"+"&end_date="+EndDate+" 23:59:59"+"&to_id="+userId, mQueue!!, token!!, object :
                ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null){
                            progressBarHome.visibility = View.GONE
                            summaryLayout.visibility = View.VISIBLE
                            tryAgain.visibility = View.GONE
                            val obj = JSONObject(response)
                            val ordersArray = obj.getJSONArray("so_list")
                            val itemList: ArrayList<Pair<String, String>> = ArrayList()
                            if (ordersArray.length() >0){
                                for(i in 0 until ordersArray.length()){
                                    val orderObj = ordersArray.getJSONObject(i)
                                    itemList.add(Pair(orderObj.getString("sr_name"), dformat.format(orderObj.getString("so_ordered_value").toDouble())))
                                }
                            }
                            if (!obj.getString("order_amount").equals("null")) ovCount.setText(dformat.format(obj.getString("order_amount").toDouble()))
                            if (!obj.getString("sku_per_memo").equals("null")) bpcCount.setText(dformat.format(obj.getString("sku_per_memo").toDouble()))
                            if (!obj.getString("number_of_memo").equals("null")) lpcCount.setText(dformat.format(obj.getString("number_of_memo").toDouble()))
                            createTable(itemList, tabLayout2)

                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        progressBarHome.visibility = View.GONE
                        summaryLayout.visibility = View.GONE
                        tryAgain.visibility = View.VISIBLE
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    TODO("Not yet implemented")
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    TODO("Not yet implemented")
                }

                override fun onResponseFailure(error: VolleyError) {
                    try{
                        ViewUtils.getErrorResponse(error, mContext!!)
                        progressBarHome.visibility = View.GONE
                        summaryLayout.visibility = View.GONE
                        tryAgain.visibility = View.VISIBLE
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }

                override fun onException(e: Exception) {
                    try{
                        progressBarHome.visibility = View.GONE
                        summaryLayout.visibility = View.GONE
                        tryAgain.visibility = View.VISIBLE
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }

            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun createTable(data: ArrayList<Pair<String, String>>, tab_Layout: TableLayout) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        var size : Int = 0
        if (data.size<5){
            size = data.size
        }else{
            size = 5
        }
        for (i in 0 until size) {
            val tr = TableRow(mContext)
            val tableRowParams = TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
            val leftMargin = 0
            val topMargin = 0
            val rightMargin = 0
            val bottomMargin = 8

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
            tr.setLayoutParams(tableRowParams)
            tr.gravity = Gravity.CENTER_VERTICAL
            val c1 = TextView(mContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(mContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            c2.gravity = Gravity.CENTER
            c2.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            tr.addView(c1)
            tr.addView(c2)
            tab_Layout.addView(tr)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        employeeId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        territoryId = prefs!!.getString(Api.TERRITORY_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
    }
}