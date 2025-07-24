package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.StatisticsHome.Fragment.adapter.AdapterTodayCategory
import com.barikoi.cnlapp.StatisticsHome.Model.Categories
import com.barikoi.cnlapp.databinding.FragmentLastWeekCategoryBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class LastWeekCategoryFragment : Fragment() {
    private lateinit var binding: FragmentLastWeekCategoryBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var mQueue: RequestQueue

    private lateinit var adapterTodayCategory: AdapterTodayCategory


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLastWeekCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapterTodayCategory = AdapterTodayCategory()
        binding.rcvTodayCategory.layoutManager = LinearLayoutManager(requireContext())
        binding.rcvTodayCategory.adapter = adapterTodayCategory
        val dividerItemDecoration = DividerItemDecoration(
            binding.rcvTodayCategory.context,
            LinearLayoutManager.VERTICAL // অথবা HORIZONTAL
        )
        binding.rcvTodayCategory.addItemDecoration(dividerItemDecoration)
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
        getSummaryCategory(
            Api.get_last_week_category + "?user_id=" + sharePrefUtils.getString(Api.USER_ID) + "&route_id=" + sharePrefUtils.getString(
                Api.SELECTED_ROUTE_ID
            ) + "&last_week_category=1"
        )
    }

    private fun getSummaryCategory(url: String) {
        ApiServices.apiGET(
            url,
            mQueue,
            sharePrefUtils.getString(Api.TOKEN)!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        binding.progressBar.visibility = View.GONE
                        val dFormat = DecimalFormat("#.##")
                        val itemList: ArrayList<Categories> = ArrayList()
                        val obj = JSONObject(response)
                        val categoryArray = obj.getJSONArray("outlet_categories")
                        if (categoryArray.length() > 0) {
                            for (i in 0 until categoryArray.length()) {
                                val productObj = categoryArray.getJSONObject(i)
                                if (!productObj.getString("outlet_category")
                                        .equals("") && !productObj.getString("outlet_category")
                                        .equals("null")
                                ) {
                                    val outletCatName = productObj.getString("outlet_category")
                                    val outletCount = productObj.getString("total_outlet")
                                    val orderDone =
                                        productObj.getString("outlet_count_delivered")
                                    val orderValue = dFormat.format(
                                        productObj.getString("delivery_value").toDouble()
                                    )
                                    val sumOutletCount =
                                        productObj.getString("sum_total_outlet")
                                    val sumOrderDone =
                                        productObj.getString("sum_outlet_count_delivered")
                                    val sumOrderValue = dFormat.format(
                                        productObj.getString("sum_delivery_value").toDouble()
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
//                        createTable(itemList)
                        adapterTodayCategory.updateCategories(itemList)


                    } catch (e: Exception) {
                        e.printStackTrace()
                        binding.progressBar.visibility = View.GONE
                    }

                }

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, requireContext())
                    binding.progressBar.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    binding.progressBar.visibility = View.GONE
                }

            })
    }

}