package com.barikoi.cnlapp.Order_Delivery

import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Order_Delivery.Fragments.BouncedOrderFragment
import com.barikoi.cnlapp.Order_Delivery.Fragments.DeliveredOrderFragment
import com.barikoi.cnlapp.Order_Delivery.Fragments.PendingOrderFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_order_delivery_update.*
import kotlinx.android.synthetic.main.activity_order_delivery_update.btnBack
import kotlinx.android.synthetic.main.activity_order_delivery_update.dateRangeLayout
import kotlinx.android.synthetic.main.activity_order_delivery_update.tvDateRange
import kotlinx.android.synthetic.main.activity_order_delivery_update.viewPager
import kotlinx.android.synthetic.main.activity_order_delivery_update.viewpagertab
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class OrderDeliveryUpdateActivity : AppCompatActivity() {

    var token : String? = null
    var user_id : String? = null
    var sr_id : String? = null
    var territory_id : String? = null
    var user_type : String? = null
    var route_id: String? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var queue: RequestQueue? = null
    companion object{
        var StartDate: String? = null
        var EndDate: String? = null
        var etSearchShop: AutoCompleteTextView? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_delivery_update)
        queue = RequestQueueSingleton.getInstance(applicationContext).getRequestQueue()
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        token = prefs!!.getString(Api.TOKEN, "")
        user_id = prefs!!.getString(Api.USER_ID, "")
        user_type = prefs!!.getString(Api.USER_TYPE, "")
        //sr_id = prefs!!.getString(Api.SR_CODE, "")
        //route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
        territory_id = prefs!!.getString(Api.TERRITORY_ID, "")

        etSearchShop = findViewById(R.id.editTextSearchShop)

        if (user_type.equals("TO", true)){
            sr_id = ""
            route_id = ""
        }else{
            sr_id = prefs!!.getString(Api.EMPLOYEE_ID, "")
            route_id = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")
        }
        btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
        setDateFilter()

    }

    private fun setTabLayoutView() {
        val titles = arrayOf(resources.getString(R.string.pending), resources.getString(R.string.delivered), resources.getString(R.string.bounced))
        val fragments = ArrayList<Fragment>()
        fragments.add(PendingOrderFragment())
        fragments.add(DeliveredOrderFragment())
        fragments.add(BouncedOrderFragment())

        viewPager.setAdapter(ViewPagerAdapter(supportFragmentManager, lifecycle, fragments))
        TabLayoutMediator(viewpagertab, viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()

        viewPager.setUserInputEnabled(false)
        for (i in 0 until viewpagertab.getTabCount()) {
            val tab = (viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + viewPager.getCurrentItem())
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    viewPager.setCurrentItem(0)
                    PendingOrderFragment.checkforOrders(queue!!, token!!, user_id!!, sr_id!!, route_id!!, territory_id!!, StartDate!!, EndDate!!)
                } else if (position == 1) {
                    viewPager.setCurrentItem(1)
                    DeliveredOrderFragment.checkforOrders(queue!!, token!!, user_id!!, sr_id!!, route_id!!, territory_id!!, StartDate!!, EndDate!!)
                }else if (position == 2) {
                    viewPager.setCurrentItem(2)
                    BouncedOrderFragment.checkforOrders(queue!!, token!!, user_id!!, sr_id!!, route_id!!, territory_id!!, StartDate!!, EndDate!!)
                }
            }
        })
    }

    private fun setDateFilter() {
        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.add(Calendar.DATE, -1)
        //val end = Calendar.getInstance().time
        val end = c.time
        //val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val simpleFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        StartDate = df.format(end)
        EndDate = df.format(end)

        tvDateRange.setText(/*simpleFormat.format(end) + " - " + */simpleFormat.format(end))


        val materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
        materialDateBuilder.setTitleText("SELECT A DATE")

        val materialDatePicker = materialDateBuilder.build()

        dateRangeLayout.setOnClickListener(View.OnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
            dateRangeLayout.setEnabled(false)
        })

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            dateRangeLayout.setEnabled(true)
            val s_date = Date(selection.first!!)
            val e_date = Date(selection.second!!)
            if (s_date.compareTo(e_date) == 0) {
                tvDateRange.setText(simpleFormat.format(s_date))
                editor!!.putString(Api.START_DATE_ORDER, df.format(s_date))
                editor!!.putString(Api.END_DATE_ORDER, df.format(e_date))
                editor!!.commit()
            } else {
                tvDateRange.setText(simpleFormat.format(s_date) + " - " + simpleFormat.format(e_date))
                editor!!.putString(Api.START_DATE_ORDER, df.format(s_date))
                editor!!.putString(Api.END_DATE_ORDER, df.format(e_date))
                editor!!.commit()
            }
            StartDate = df.format(s_date)
            EndDate = df.format(e_date)
            setTabLayoutView()

        }

        materialDatePicker.addOnNegativeButtonClickListener { dateRangeLayout.setEnabled(true) }
        setTabLayoutView()
    }
}