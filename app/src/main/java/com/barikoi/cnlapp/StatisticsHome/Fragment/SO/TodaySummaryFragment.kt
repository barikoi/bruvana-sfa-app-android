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
import androidx.core.content.ContextCompat
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
import com.barikoi.cnlapp.utils.extension.format
import org.json.JSONObject

class TodaySummaryFragment : Fragment() {
    private lateinit var binding: FragmentTodaysSummaryBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = null
    var srId: String? = ""
    var userId: String? = ""
    var routeId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodaysSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSummaryTargets(Api.get_summary + "?today_summary=1&user_id=" + userId)
    }

    private fun getSummaryTargets(url: String) {
        var totalTargetCompleted = "--:--"
        var lpcCompleted = "--:--"
        var bpcCompleted = "--:--"
        var aivCompleted = "--:--"
        var visitRatio = "--:--"
        var visitCovered = "--:--"

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    binding.progressBar.visibility = View.GONE
                    val obj = JSONObject(response)
                    val completedArray = obj.getJSONArray("today_summary")
                    if (completedArray.length() > 0) {
                        for (i in 0 until completedArray.length()) {
                            val targetObj = completedArray.getJSONObject(i)
                            if (!targetObj.isNull("revenue")) totalTargetCompleted =
                                targetObj.getString("revenue").toDouble().toString().format()
                            if (!targetObj.isNull("sku_per_memo")) bpcCompleted =
                                targetObj.getString("sku_per_memo").toDouble().toString().format()
                            if (!targetObj.isNull("number_of_memo")) lpcCompleted =
                                targetObj.getString("number_of_memo").toDouble().toString().format()
                            if (!targetObj.isNull("aiv")) aivCompleted =
                                targetObj.getString("aiv").toDouble().toString().format()
                            if (!targetObj.isNull("number_of_visits")) visitRatio =
                                targetObj.getString("number_of_visits").toDouble().toString()
                                    .format()
                            if (!targetObj.isNull("distance_from_outlets")) visitCovered =
                                targetObj.getString("distance_from_outlets").toDouble().toString()
                                    .format()
                        }
                    }

                    val itemList: ArrayList<Pair<String, String>> = ArrayList()
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.total_order_value),
                            totalTargetCompleted
                        )
                    )
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.sku_per_memo),
                            bpcCompleted
                        )
                    )
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.visit_ratio),
                            visitRatio
                        )
                    )
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.visit_500m),
                            visitCovered
                        )
                    )
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.number_of_memo),
                            lpcCompleted
                        )
                    )
                    itemList.add(
                        Pair(
                            mContext!!.resources.getString(R.string.aiv),
                            aivCompleted
                        )
                    )

                    createTable(itemList)


                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.progressBar.visibility = View.GONE
                }

            }

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

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
            c1.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_title
                )
            )
            c1.text = data[i].first
            val c2 = TextView(mContext)
            c2.gravity = Gravity.END
            c2.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.text_title
                )
            )
            c2.text = data[i].second
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