package com.barikoi.cnlapp.Attendance

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.RequestQueue
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Attendance.Fragment.CreateAttendanceFragment
import com.barikoi.cnlapp.Attendance.Fragment.SO.HistoryFragment
import com.barikoi.cnlapp.Attendance.Fragment.SO.SummaryFragment
import com.barikoi.cnlapp.Attendance.Fragment.TO.HistoryTOFragment
import com.barikoi.cnlapp.Attendance.Fragment.TO.SummaryTOFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentAttendanceBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AttendanceFragment : Fragment() {
    private lateinit var binding: FragmentAttendanceBinding

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String? = null
    var user_type: String? = null

    companion object {
        var viewPager2: ViewPager2? = null
        var viewpagertab2: TabLayout? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager2 = view.findViewById(R.id.viewPager)
        viewpagertab2 = view.findViewById(R.id.viewpagertab)
        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return  binding.root
    }

    private fun init() {
        val titles = arrayOf(
            resources.getString(R.string.attendance),
            resources.getString(R.string.history),
            resources.getString(R.string.summary)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(CreateAttendanceFragment())
        if (user_type.equals("TO", true)) {
            fragments.add(HistoryTOFragment())
            fragments.add(SummaryTOFragment())
        } else {
            fragments.add(HistoryFragment())
            fragments.add(SummaryFragment())
        }
        viewPager2!!.setAdapter(ViewPagerAdapter(childFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(viewpagertab2!!, viewPager2!!,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()
        viewPager2!!.currentItem = 0;

        viewPager2!!.setUserInputEnabled(false)
        for (i in 0 until binding.viewpagertab.tabCount) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + viewPager2!!.getCurrentItem())
        viewPager2!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()*/
                } else if (position == 1) {

                    //HistoryFragment.setDateFilter(mQueue!!, token!!, start, end)
                }
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
        user_type = prefs!!.getString(Api.USER_TYPE, "")
    }


}