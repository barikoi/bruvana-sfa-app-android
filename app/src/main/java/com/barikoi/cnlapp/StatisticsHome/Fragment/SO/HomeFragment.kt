package com.barikoi.cnlapp.StatisticsHome.Fragment.SO

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.*
import com.barikoi.cnlapp.Activity.MainActivity.Companion.routeName_selected
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Adapter.TargetAdapter
import com.barikoi.cnlapp.StatisticsHome.Model.TargetValue
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.tvDateRange
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    val dots: ArrayList<ImageView> = ArrayList()
    var token: String ? = ""
    var srId: String ? = ""
    var userId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkforAttendanceToday()

    }

    private fun checkforAttendanceToday() {
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        ApiServices.apiGET(Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today),
            mQueue!!, token!!, object : ApiServiceListener{
                override fun onResponseSuccess(response: String) {
                    try {
                        if (response != null) {
                            val obj = JSONObject(response)
                            val attedanceArray = obj.getJSONArray("attendances")
                            if (attedanceArray.length() >0){
                                no_route_check.visibility = View.GONE
                                bodyLayout.visibility = View.VISIBLE
                                val attendanceObj = attedanceArray.getJSONObject(0)
                                if (!attendanceObj.getString("route_id").equals("null")) {
                                    attendanceObj.getInt("route_id")
                                    attendanceObj.getString("route_name")
                                    editor!!.putString(
                                        Api.SELECTED_ROUTE_ID,
                                        attendanceObj.getInt("route_id").toString()
                                    )
                                        .putString(
                                            Api.SELECTED_ROUTE_NAME,
                                            attendanceObj.getString("route_name")
                                        ).commit()
                                    if (attendanceObj.getString("route_name").length > 0) {
                                        routeName_selected!!.visibility = View.VISIBLE
                                        routeName_selected!!.setText(attendanceObj.getString("route_name"))
                                    }else{
                                        routeName_selected!!.visibility = View.GONE
                                    }
                                    routeId =  attendanceObj.getInt("route_id").toString()
                                    init()
                                }else{
                                    no_route_check.visibility = View.VISIBLE
                                    bodyLayout.visibility = View.GONE

                                    btn_tryAgain.setOnClickListener {
                                        checkforAttendanceToday()
                                    }
                                }

                            }else{
                                no_route_check.visibility = View.VISIBLE
                                bodyLayout.visibility = View.GONE

                                btn_tryAgain.setOnClickListener {
                                    checkforAttendanceToday()
                                }
                            }
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
                    Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun init() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time
        //val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("LLL dd", Locale.ENGLISH)
        tvDateRange.setText(simpleFormat.format(start) + " - " + simpleFormat.format(end))
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayoutHome.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayoutHome.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayoutHome.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(s_date))
                editor!!.commit()
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                editor!!.putString(Api.START_DATE_ATTENDANCE, df.format(s_date))
                editor!!.putString(Api.END_DATE_ATTENDANCE, df.format(e_date))
                editor!!.commit()
            }

            getSummaryTargets(Api.get_summary+"?start_date="+df.format(s_date)+" 00:00:00"+"&end_date="+df.format(e_date)+" 23:59:59"+"&user_id="+userId/*+"&route_id="+routeId*/)
        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayoutHome.setEnabled(true) }

        getSummaryTargets(Api.get_summary+"?start_date="+StartDate+" 00:00:00"+"&end_date="+EndDate+" 23:59:59"+"&user_id="+userId/*+"&route_id="+routeId*/)

        /*setSecondPartSummary()
        setThirdPartSummary()*/

    }

    private fun getSummaryTargets(url: String) {
        var total_target = "--:--"
        var total_target_completed = "--:--"
        var lpc = "--:--"
        var lpc_completed = "--:--"
        var bpc = "--:--"
        var bpc_completed = "--:--"
        var aiv = "--:--"
        var aiv_completed = "--:--"
        var ads = "--:--"
        var ads_completed = "--:--"
        var rds = "--:--"
        var rds_completed = "--:--"
        var visit_completed = "--:--"
        var visited = "--:--"
        var bounce_completed = "--:--"
        var bounced = "--:--"
        var dformat = DecimalFormat("#.##")
        ApiServices.apiGET(url, mQueue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                try {
                    if (response != null){
                        val obj = JSONObject(response)
                        val targetsArray = obj.getJSONArray("targets")
                        val completedArray = obj.getJSONArray("target_completed")
                        if (targetsArray.length() > 0){
                            for (i in 0 until targetsArray.length()){
                                val targetObj =targetsArray.getJSONObject(i)
                                //if(!targetObj.isNull("target_amount")) total_target = Math.round(targetObj.getString("target_amount").toDouble()).toString()
                                if(!targetObj.isNull("target_amount")) total_target = dformat.format(targetObj.getString("target_amount").toDouble())
                                if(!targetObj.isNull("target_ads")) ads = dformat.format(targetObj.getString("target_ads").toDouble())
                                if(!targetObj.isNull("target_rds")) rds = dformat.format(targetObj.getString("target_rds").toDouble())
                                if(!targetObj.isNull("target_sku_per_memo")) bpc = dformat.format(targetObj.getString("target_sku_per_memo").toDouble())
                                if(!targetObj.isNull("target_number_of_memo")) lpc = dformat.format(targetObj.getString("target_number_of_memo").toDouble())
                                if(!targetObj.isNull("target_number_of_visits")) visited = dformat.format(targetObj.getString("target_number_of_visits").toDouble())
                                if(!targetObj.isNull("target_aiv")) aiv = dformat.format(targetObj.getString("target_aiv").toDouble())
                                if(!targetObj.isNull("threshold_bounce_percentage")) bounced = dformat.format(targetObj.getString("threshold_bounce_percentage").toDouble())
                            }
                        }
                        if (completedArray.length() > 0){
                            for (i in 0 until completedArray.length()){
                                val targetObj =completedArray.getJSONObject(i)
                                //if(!targetObj.isNull("revenue")) total_target_completed = Math.round(targetObj.getString("revenue").toDouble()).toString()
                                if(!targetObj.isNull("revenue")) total_target_completed = dformat.format(targetObj.getString("revenue").toDouble())
                                if(!targetObj.isNull("ads")) ads_completed = dformat.format(targetObj.getString("ads").toDouble())
                                if(!targetObj.isNull("rds")) rds_completed = dformat.format(targetObj.getString("rds").toDouble())
                                if(!targetObj.isNull("sku_per_memo")) bpc_completed = dformat.format(targetObj.getString("sku_per_memo").toDouble())
                                if(!targetObj.isNull("number_of_memo")) lpc_completed = dformat.format(targetObj.getString("number_of_memo").toDouble())
                                if(!targetObj.isNull("number_of_visits")) visit_completed = dformat.format(targetObj.getString("number_of_visits").toDouble())
                                if(!targetObj.isNull("aiv")) aiv_completed = dformat.format(targetObj.getString("aiv").toDouble())
                                if(!targetObj.isNull("bounce_quantity_percentage")) bounce_completed = dformat.format(targetObj.getString("bounce_quantity_percentage").toDouble())
                            }
                        }

                        val itemList: ArrayList<TargetValue> = ArrayList()
                        itemList.add(TargetValue(resources.getString(R.string.total_target), total_target, total_target_completed))
                        itemList.add(TargetValue(resources.getString(R.string.ads), ads, ads_completed))
                        itemList.add(TargetValue(resources.getString(R.string.rds), rds, rds_completed))
                        itemList.add(TargetValue(resources.getString(R.string.sku_per_memo), bpc, bpc_completed))
                        itemList.add(TargetValue(resources.getString(R.string.number_of_memo), lpc, lpc_completed))
                        itemList.add(TargetValue(resources.getString(R.string.visit_ratio), visited, visit_completed))
                        itemList.add(TargetValue(resources.getString(R.string.aiv), aiv, aiv_completed))
                        itemList.add(TargetValue(resources.getString(R.string.bounce)+" (%)", bounced, bounce_completed))

                        val adapter = TargetAdapter(itemList, "SO")
                        targetListView.adapter = adapter
                        adapter.notifyDataSetChanged()
                        setSecondPartSummary()
                        //setThirdPartSummary()

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

    private fun setSecondPartSummary() {
        layoutSecond.visibility = View.VISIBLE
        val titles = arrayOf(resources.getString(R.string.today_summary),resources.getString(R.string.last_week_summary), resources.getString(R.string.last_week_product), resources.getString(R.string.last_week_category), resources.getString(R.string.last_week_delivery), resources.getString(R.string.bounce_list))
        val fragments = ArrayList<Fragment>()
        fragments.add(TodaysSummaryFragment())
        fragments.add(LastWeekSummaryFragment())
        fragments.add(LastWeekProductFragment())
        fragments.add(LastWeekCategoryFragment())
        fragments.add(LastWeekDeliveryFragment())
        fragments.add(LastWeekBounceFragment())
        addDots(fragments.size)
        viewPager.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(viewpagertab, viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()
        viewPager.setCurrentItem(0);

        //viewPager.setUserInputEnabled(false)
        for (i in 0 until viewpagertab.getTabCount()) {
            val tab = (viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + viewPager.getCurrentItem())
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                selectDot(position, fragments.size)
                if (position == 0) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()*/
                } else if (position == 1) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                }
                else if (position == 2) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                }
            }
        })
    }

    private fun setThirdPartSummary() {
        layoutThird.visibility = View.VISIBLE
        val titles = arrayOf(resources.getString(R.string.last_week_delivery), resources.getString(R.string.bounce_list))
        val fragments = ArrayList<Fragment>()
        fragments.add(LastWeekDeliveryFragment())
        fragments.add(LastWeekBounceFragment())
        viewPagerSecond.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(viewpagertabSecond, viewPagerSecond,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()
        viewPagerSecond.setCurrentItem(0);

        viewPagerSecond.setUserInputEnabled(false)
        for (i in 0 until viewpagertabSecond.getTabCount()) {
            val tab = (viewpagertabSecond.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + viewPagerSecond.getCurrentItem())
        viewPagerSecond.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()*/
                } else if (position == 1) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                }
                else if (position == 2) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()*/
                }
            }
        })
    }

    fun addDots(dotCount: Int) {

        for (i in 0 until dotCount) {
            val dot = ImageView(mContext)
            dot.setImageDrawable(resources.getDrawable(R.drawable.ic_dot_unselected))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(2)
            dotsLayout.addView(dot, params)
            dots.add(dot)
        }
    }
    fun selectDot(idx: Int, dotCount: Int) {
        val res: Resources = resources
        for (i in 0 until dots.size) {
            val drawableId: Int =
                if (i == idx) com.barikoi.cnlapp.R.drawable.ic_dot_selected else com.barikoi.cnlapp.R.drawable.ic_dot_unselected
            val drawable: Drawable = res.getDrawable(drawableId)
            dots.get(i).setImageDrawable(drawable)
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