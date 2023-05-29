package com.barikoi.cnlapp.Adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, fragmnts: ArrayList<Fragment>) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    var fragments = fragmnts

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment? = null
        Log.d("Fragment", "fragment tab pos: $position")
        fragment = fragments[position]
        return fragment
    }
}