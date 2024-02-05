package com.barikoi.cnlapp.StatisticsHome.Fragment.TO

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.bpcCount
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.lpcCount
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.ovCount
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.progressBarHome
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.summaryLayout
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.tabLayout2
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.tabLayoutTarget
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.targetLayout
import kotlinx.android.synthetic.main.fragment_todays_summary_t_o.tryAgain
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TodaysSummaryTOFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String ? = ""
    var territoryId: String ? = ""
    var employeeId: String? = ""
    var userId: String? = ""
    var pd: ProgressDialog? = null
    var EndDate: String? = null
    var soId: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_todays_summary_t_o, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTodaysSummary()

        tryAgain.setOnClickListener {
            setTodaysSummary()
        }
    }

    private fun setTodaysSummary() {
        try{
            progressBarHome.visibility = View.VISIBLE
            summaryLayout.visibility = View.GONE
            val dformat = DecimalFormat("#.##")
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_WEEK, -7)
            val end = Calendar.getInstance().time
            val start = c.time
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val simpleFormat = SimpleDateFormat("LLL dd", Locale.ENGLISH)
            //tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
            val StartDate = df.format(start)
            EndDate = df.format(end)
            ApiServices.apiGET(Api.get_all_so_list+"?today_summary=1&start_date="+EndDate+" 00:00:00"+"&end_date="+EndDate+" 23:59:59"+"&to_id="+userId, mQueue!!, token!!, object :
                ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null){
                            progressBarHome.visibility = View.GONE
                            summaryLayout.visibility = View.VISIBLE
                            tryAgain.visibility = View.GONE
                            val obj = JSONObject(response)
                            val toArray = obj.getJSONArray("so_list")
                            val toObj = toArray.getJSONObject(0)
                            val ordersArray = toObj.getJSONArray("sales_officers")
                            val itemList: ArrayList<Pair<Pair<String, String>, String>> = ArrayList()
                            if (ordersArray.length() >0){
                                for(i in 0 until ordersArray.length()){
                                    val orderObj = ordersArray.getJSONObject(i)
                                    itemList.add(
                                        Pair(
                                            Pair(
                                                orderObj.getString("user_name"),
                                                orderObj.getString("id")
                                            ),
                                            dformat.format(
                                                orderObj.getString("so_ordered_value").toDouble()
                                            )
                                        )
                                    )
                                }
                            }
                            if (!toObj.getString("order_amount").equals("null")) ovCount.setText(dformat.format(toObj.getString("order_amount").toDouble()))
                            if (!toObj.getString("sku_per_memo").equals("null")) bpcCount.setText(dformat.format(toObj.getString("sku_per_memo").toDouble()))
                            if (!toObj.getString("number_of_memo").equals("null")) lpcCount.setText(dformat.format(toObj.getString("number_of_memo").toDouble()))
                            createTableClickable(itemList, tabLayout2)

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
        if (data.size > 0) {
            /*val trefresh = TableRow(mContext)
            val refresh = TextView(mContext)
            refresh.gravity = Gravity.CENTER
            refresh.setTextColor(resources.getColor(R.color.text_title))
            refresh.setText("Refresh")
            refresh.background = resources.getDrawable(R.drawable.button_whitebg_stroke)
            refresh.setOnClickListener {
                getSummaryTargets(Api.get_summary + "?start_date=" + EndDate + " 00:00:00" + "&end_date=" + EndDate + " 23:59:59" + "&user_id=" + soId)
            }
            trefresh.addView(refresh)
            tab_Layout.addView(trefresh)*/
            //bodyLayoutScroll.smoothScrollTo(0, 0)
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
                tab_Layout.addView(tr)
            }
        }
        //getAllOrders(orderArray!!)
        pd!!.dismiss()
    }
    private fun createTableClickable(
        data: ArrayList<Pair<Pair<String, String>, String>>,
        tab_Layout: TableLayout
    ) {
        tab_Layout.isStretchAllColumns = true
        tab_Layout.bringToFront()
        tab_Layout.removeAllViews()
        tabLayoutTarget.removeAllViews()
        targetLayout.visibility = View.GONE
        if (data.size > 0) {
            for (i in 0 until data.size) {
                val tr = TableRow(mContext)
                val tableRowParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                val leftMargin = 0
                val topMargin = 0
                val rightMargin = 0
                val bottomMargin = 8

                tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
                tr.setLayoutParams(tableRowParams)
                tr.gravity = Gravity.CENTER_VERTICAL
                tr.tag = i
                val c1 = TextView(mContext)
                c1.gravity = Gravity.START
                c1.setTextColor(resources.getColor(R.color.text_title))
                c1.setText(data.get(i).first.first)
                val c2 = TextView(mContext)
                c2.gravity = Gravity.END
                c2.setTextColor(resources.getColor(R.color.text_title))
                c2.setText(data.get(i).second)
                c2.gravity = Gravity.CENTER
                c2.background = resources.getDrawable(R.drawable.button_white_bg_stroke)
                tr.addView(c1)
                tr.addView(c2)

                tr.setOnClickListener {
                    Log.d("OrderSummary", "pd.isShowing: " + pd!!.isShowing)
                    //targetLayout.visibility = View.GONE
                    pd!!.show()
                    /*Thread {
                        requireActivity().runOnUiThread(object : Runnable{
                            override fun run() {

                            }

                        })
                    }.start()*/

                    try {
                        soId = data[i].first.second
                        getSummaryTargets(Api.get_summary + "?today_summary=1&today_for_so=1&user_id=" + data[i].first.second)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Log.d("OrderSummary", "row count: " + tab_Layout.childCount)
                            for (t in 0 until tab_Layout.childCount) {
                                if (tab_Layout.getChildAt(t).tag == it.tag) {
                                    tab_Layout.getChildAt(t)
                                        .setBackgroundColor(resources.getColor(R.color.light_yellow))
                                } else {
                                    tab_Layout.getChildAt(t)
                                        .setBackgroundColor(resources.getColor(R.color.white))
                                }
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }


                }
                tab_Layout.addView(tr)
            }
        }
        pd!!.dismiss()
    }
    private fun getSummaryTargets(url: String) {
        var total_target_completed = "--:--"
        var lpc_completed = "--:--"
        var bpc_completed = "--:--"
        var aiv_completed = "--:--"
        var visit_completed = "--:--"
        var visit_covered = "--:--"
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null) {
                        val obj = JSONObject(response)
                        val completedArray = obj.getJSONArray("today_summary")
                        progressBarHome.visibility = View.GONE
                        targetLayout.visibility = View.VISIBLE
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("today_ordered_amount")) total_target_completed =
                                    dformat.format(targetObj.getString("today_ordered_amount").toDouble())
                                if (!targetObj.isNull("sku_per_memo")) bpc_completed =
                                    dformat.format(targetObj.getString("sku_per_memo").toDouble())
                                if (!targetObj.isNull("number_of_memo")) lpc_completed =
                                    dformat.format(targetObj.getString("number_of_memo").toDouble())
                                if (!targetObj.isNull("number_of_visits")) visit_completed =
                                    dformat.format(
                                        targetObj.getString("number_of_visits").toDouble()
                                    )
                                if(!targetObj.isNull("distance_from_outlets")) visit_covered = dformat.format(targetObj.getString("distance_from_outlets").toDouble())
                                if (!targetObj.isNull("aiv")) aiv_completed =
                                    dformat.format(targetObj.getString("aiv").toDouble())
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(
                            Pair(
                                resources.getString(R.string.total_order_value),
                                total_target_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.sku_per_memo),
                                bpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.number_of_memo),
                                lpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                resources.getString(R.string.visit_ratio),
                                visit_completed
                            )
                        )
                        itemList.add(Pair(resources.getString(R.string.visit_500m), visit_covered))
                        itemList.add(Pair(resources.getString(R.string.aiv), aiv_completed))

                        createTable(itemList, tabLayoutTarget)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBarHome.visibility = View.GONE
                    //getAllOrders(orderArray!!)
                    pd!!.dismiss()
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
                progressBarHome.visibility = View.GONE
                //getAllOrders(orderArray!!)
                pd!!.dismiss()
            }

            override fun onException(e: Exception) {
                progressBarHome.visibility = View.GONE
                //getAllOrders(orderArray!!)
                pd!!.dismiss()
            }

        })

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
        pd = ProgressDialog(mContext)
        pd!!.setMessage("Processing...")
        pd!!.setCancelable(false)
    }

    override fun onDetach() {
        super.onDetach()
        setTodaysSummary()
    }
}