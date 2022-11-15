package com.barikoi.cnlapp.Order_Create.Fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.callback.OnBackPressedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_create_order.*
import java.io.Serializable

class CreateOrderFragment : Fragment(), OnBackPressedListener{
    lateinit var ACTIVITY: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ACTIVITY = context as MainActivity
        //setCurrentFragment(ShopSelectFragment(), ACTIVITY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTabViewPager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    /*fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }*/

    private fun setTabViewPager() {
        val titles = arrayOf(resources.getString(R.string.selec_dokan), resources.getString(R.string.confirm_order))
        val fragments = ArrayList<Fragment>()
        fragments.add(SelectDokanFragment())
        fragments.add(ConfirmOrderFragment())
        viewPager.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(viewpagertab, viewPager,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()
        viewPager.setCurrentItem(0);

        viewPager.setUserInputEnabled(false)
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


    companion object{
        fun startFragmentWithValue(
            key: String,
            value: Serializable,
            fragmentName: Fragment,
            activity: Activity
        ) {
            val bundle = Bundle()
            bundle.putString("from", key)
            bundle.putSerializable(key, value) // Put anything what you want

            fragmentName.arguments = bundle
            val fragmentManager = (activity as FragmentActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragmentLayout2, fragmentName)
                .commit()
        }

        fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
            val fragmentManager = (activity as FragmentActivity).supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentLayout2, fragment!!)
            fragmentTransaction.commit()
            //fragmentManager.executePendingTransactions()
        }
    }

    override fun onBackPressed() {
        /*val fragment =
            this.supportFragmentManager.findFragmentById(R.id.main_container)
        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {

        }*/
    }


}