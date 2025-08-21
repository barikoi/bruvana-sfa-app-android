package com.barikoi.cnlapp.ui.attendance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.fragment.app.Fragment
import com.barikoi.cnlapp.Attendance.Fragment.CreateAttendanceFragment
import com.barikoi.cnlapp.Attendance.Fragment.SO.HistoryFragment
import com.barikoi.cnlapp.Attendance.Fragment.SO.SummaryFragment
import com.barikoi.cnlapp.Attendance.Fragment.TO.HistoryTOFragment
import com.barikoi.cnlapp.Attendance.Fragment.TO.SummaryTOFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.databinding.FragmentAttendanceBinding
import com.barikoi.cnlapp.ui.adapter.ViewPagerAdapter
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceFragment : Fragment() {
    private lateinit var binding: FragmentAttendanceBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val titles = arrayOf(
            resources.getString(R.string.attendance),
            resources.getString(R.string.history),
            resources.getString(R.string.summary)
        )
        val fragments = ArrayList<Fragment>()
        fragments.add(CreateAttendanceFragment())
        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true) ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM", true)
        ) {
            fragments.add(HistoryTOFragment())
            fragments.add(SummaryTOFragment())
        } else {
            fragments.add(HistoryFragment())
            fragments.add(SummaryFragment())
        }

        binding.viewPager.setAdapter(ViewPagerAdapter(childFragmentManager, lifecycle, fragments))

        TabLayoutMediator(
            binding.viewpagertab, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = titles[position]
        }.attach()
        binding.viewPager.currentItem = 0

        binding.viewPager.setUserInputEnabled(false)
        for (i in 0 until binding.viewpagertab.tabCount) {
            val tab = (binding.viewpagertab.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(15, 15, 10, 15)
            tab.requestLayout()
        }

    }
}