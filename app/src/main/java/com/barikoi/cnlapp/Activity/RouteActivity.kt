package com.barikoi.cnlapp.Activity

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Fragment.RouteFragment
import com.barikoi.cnlapp.Fragment.ShopListFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy

class RouteActivity : AppCompatActivity() {
    var tabLayout2: TabLayout? = null
    var viewPager2: ViewPager2? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueueSingleton? = null
    private var userId: String? = ""
    private var srCode: String? = ""
    private var tvTitle: TextView? = null
    private var back_img: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()

        userId = prefs!!.getString(Api.USER_ID, "")
        srCode = prefs!!.getString(Api.SR_CODE, "")

        mQueue = RequestQueueSingleton.getInstance(applicationContext)

        back_img = findViewById(R.id.btnBack)

        back_img!!.setOnClickListener {
            onBackPressed()
            finish()
        }

        tvTitle = findViewById(R.id.tvTitle)
        viewPager2 = findViewById<ViewPager2>(R.id.viewPager)
        tabLayout2 = findViewById<TabLayout>(R.id.viewpagertab)

        val titles = arrayOf(resources.getString(R.string.route_list), resources.getString(R.string.shop_list))
        val fragments = ArrayList<Fragment>()
        fragments.add(RouteFragment())
        fragments.add(ShopListFragment())
        viewPager2!!.setAdapter(ViewPagerAdapter(supportFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        // attaching tab mediator
        TabLayoutMediator(tabLayout2!!, viewPager2!!,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()
        //viewPager.setCurrentItem(0);

        //viewPager.setCurrentItem(0);
        viewPager2!!.setUserInputEnabled(false)

        Log.d("Fragment", "viewpager current Item: " + viewPager2!!.getCurrentItem())
        viewPager2!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()
                    tvTitle!!.text = resources.getString(R.string.route_list)
                    RouteFragment.getAllRouteList(userId!!)
                } else if (position == 1) {
                    editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()
                    tvTitle!!.text = resources.getString(R.string.shop_list)
                    ShopListFragment.getShopList(userId!!)
                }
            }
        })

    }
}