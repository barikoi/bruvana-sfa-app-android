package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import kotlinx.android.synthetic.main.fragment_last_week_category.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class LastWeekCategoryFragment : Fragment() {
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token : String? = null
    var srId: String ? = ""
    var userId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_last_week_category, container, false)
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
        getSummaryCategory(Api.get_last_week_category+"?start_date="+StartDate+" 00:00:00"+"&end_date="+EndDate+"&user_id="+userId+"&route_id="+routeId)
    }

    private fun getSummaryCategory(url: String) {

        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val itemList: ArrayList<Pair<String, String>> = ArrayList()
                        val obj = JSONObject(response)
                        val categoryArray = obj.getJSONArray("outlet_categories")
                        if (categoryArray.length() > 0){
                            for (i in 0 until categoryArray.length()){
                                val productObj = categoryArray.getJSONObject(i)
                                if (!productObj.getString("outlet_category").equals("") && !productObj.getString("outlet_category").equals("null")) {
                                    val outletCatName = productObj.getString("outlet_category")
                                    val outletCount = productObj.getString("outlet_count")
                                    itemList.add(Pair(outletCatName, outletCount))
                                }
                            }
                        }
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
                ViewUtils.getErrorResponse(error, mContext!!)
            }

            override fun onException(e: Exception) {
                TODO("Not yet implemented")
            }

        })

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createTable(data: ArrayList<Pair<String, String>>) {
        tabLayout.isStretchAllColumns = true
        tabLayout.bringToFront()
        val colorsTxt: Array<String> = mContext!!.getResources().getStringArray(R.array.colors)
        for (i in 0 until data.size) {
            val tr = TableRow(mContext)
            val image = ImageView(mContext)
            image.setImageDrawable(resources.getDrawable(R.drawable.ic_dot))
            image.drawable.setTint(Color.parseColor(colorsTxt[i]))
            val c1 = TextView(mContext)
            c1.gravity = Gravity.START
            c1.setTextColor(resources.getColor(R.color.text_title))
            c1.setText(data.get(i).first)
            val c2 = TextView(mContext)
            c2.gravity = Gravity.END
            c2.setTextColor(resources.getColor(R.color.text_title))
            c2.setText(data.get(i).second)
            tr.addView(image)
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
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
    }

}