package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentTodaysSummaryBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TodaysSummaryFragment : Fragment() {
    private lateinit var binding: FragmentTodaysSummaryBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = null
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""
    lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTodaysSummaryBinding.inflate(inflater, container, false)
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
        getSummaryTargets(Api.get_summary + "?today_summary=1&user_id=" + userId + "&route_id=" + routeId)
    }

    private fun getSummaryTargets(url: String) {
        var total_target_completed = "--:--"
        var lpc_completed = "--:--"
        var bpc_completed = "--:--"
        var aiv_completed = "--:--"
        var visit_ratio = "--:--"
        var visit_covered = "--:--"

        var dformat = DecimalFormat("#.##")

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    binding.progressBar.visibility = View.GONE
                    if (response != null) {
                        val obj = JSONObject(response)
                        val completedArray = obj.getJSONArray("today_summary")
                        if (completedArray.length() > 0) {
                            for (i in 0 until completedArray.length()) {
                                val targetObj = completedArray.getJSONObject(i)
                                if (!targetObj.isNull("revenue")) total_target_completed =
                                    dformat.format(targetObj.getString("revenue").toDouble())
                                if (!targetObj.isNull("sku_per_memo")) bpc_completed =
                                    dformat.format(targetObj.getString("sku_per_memo").toDouble())
                                if (!targetObj.isNull("number_of_memo")) lpc_completed =
                                    dformat.format(targetObj.getString("number_of_memo").toDouble())
                                if (!targetObj.isNull("aiv")) aiv_completed =
                                    dformat.format(targetObj.getString("aiv").toDouble())
                                if (!targetObj.isNull("number_of_visits")) visit_ratio =
                                    dformat.format(
                                        targetObj.getString("number_of_visits").toDouble()
                                    )
                                if (!targetObj.isNull("distance_from_outlets")) visit_covered =
                                    dformat.format(
                                        targetObj.getString("distance_from_outlets").toDouble()
                                    )
                            }
                        }

                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.total_order_value),
                                total_target_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.sku_per_memo),
                                bpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.visit_ratio),
                                visit_ratio
                            )
                        )
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.visit_500m),
                                visit_covered
                            )
                        )
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.number_of_memo),
                                lpc_completed
                            )
                        )
                        itemList.add(
                            Pair(
                                mContext!!.resources.getString(R.string.aiv),
                                aiv_completed
                            )
                        )

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


    private fun createTable(data: ArrayList<Pair<String, String>>) {
        binding.tabLayoutToday.isStretchAllColumns = true
        binding.tabLayoutToday.bringToFront()
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
            binding.tabLayoutToday.addView(tr)
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