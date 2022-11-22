package com.barikoi.cnlapp.StatisticsHome.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.android.volley.*
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils.getErrorResponse
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_last_week_summary.*
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class LastWeekSummaryFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token : String? = null
    var srId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
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
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)
        getSummaryTargets(Api.get_summary+"?start_date="+StartDate+"&end_date="+EndDate+"&sr_id="+srId+"&route_id="+routeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_week_summary, container, false)
    }

    private fun getSummaryTargets(url: String) {
        var total_target_completed = ""
        var lpc_completed = ""
        var bpc_completed = ""
        var aiv_completed = ""
        var visit_ratio = ""

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val completedArray = obj.getJSONArray("target_completed")
                        if (completedArray.length() > 0){
                            for (i in 0 until completedArray.length()){
                                val targetObj =completedArray.getJSONObject(i)
                                total_target_completed = Math.round(targetObj.getString("revenue").toDouble()).toString()
                                bpc_completed = targetObj.getString("bpc")
                                lpc_completed = targetObj.getString("lpc")
                                aiv_completed = targetObj.getString("aiv")
                                visit_ratio = targetObj.getString("visit_ratio")
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(Pair(resources.getString(R.string.total_order_value), total_target_completed))
                        itemList.add(Pair(resources.getString(R.string.sku_per_memo), bpc_completed))
                        itemList.add(Pair(resources.getString(R.string.visit_ratio), visit_ratio+"%"))
                        itemList.add(Pair(resources.getString(R.string.number_of_memo), lpc_completed))
                        itemList.add(Pair(resources.getString(R.string.aiv), aiv_completed))

                        createTable(itemList)


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
                getErrorResponse(error, mContext!!)
            }

            override fun onException(e: Exception) {
                TODO("Not yet implemented")
            }

        })

    }



    private fun createTable(data: ArrayList<Pair<String, String>>) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        srId = prefs!!.getString(Api.SR_CODE, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }

}