package com.barikoi.cnlapp.Activity


import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Attendance.Model.SOList
import com.barikoi.cnlapp.Fragment.RouteFragment
import com.barikoi.cnlapp.Fragment.ShopListFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.activity_route.*
import org.json.JSONObject

class RouteActivity : AppCompatActivity() {
    var tabLayout2: TabLayout? = null
    var viewPager2: ViewPager2? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    private var token: String? = ""

    private var srCode: String? = ""
    var selected_so : Int? = null
    private var tvTitle: TextView? = null
    val soList: ArrayList<SOList> = ArrayList()
    private var back_img: ImageButton? = null
    companion object{
        var userId: String? = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()

        token = prefs!!.getString(Api.TOKEN, "")

        if (prefs!!.getString(Api.USER_TYPE,"").equals("TO")){
            userId = ""
            srCode = ""
        }else{
            userId = prefs!!.getString(Api.USER_ID, "")
            srCode = prefs!!.getString(Api.SR_CODE, "")
        }

        mQueue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        back_img = findViewById(R.id.btnBack)

        back_img!!.setOnClickListener {
            onBackPressed()
            finish()
        }

        tvTitle = findViewById(R.id.tvTitle)
        viewPager2 = findViewById<ViewPager2>(R.id.viewPager)
        tabLayout2 = findViewById<TabLayout>(R.id.viewpagertab)

        val titles = arrayOf(resources.getString(R.string.list_tab_1), resources.getString(R.string.list_tab_2))
        val fragments = ArrayList<Fragment>()
        fragments.add(RouteFragment())
        fragments.add(ShopListFragment())
        viewPager2!!.setAdapter(ViewPagerAdapter(supportFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(tabLayout2!!, viewPager2!!,
            TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
            }).attach()

        //viewPager.setCurrentItem(0);
        viewPager2!!.setUserInputEnabled(false)

        if (prefs!!.getInt(Api.ROUTE_PAGE_SELECTED, 0) == 1) {
            viewPager2!!.setCurrentItem(1)
            tvTitle!!.text = resources.getString(R.string.shop_list)
            ShopListFragment.getShopList(userId!!)
        }else{
            viewPager2!!.setCurrentItem(0)
            tvTitle!!.text = resources.getString(R.string.route_list)
            RouteFragment.getAllRouteList(userId!!, RouteFragment.mListener!!)
        }

        Log.d("Fragment", "viewpager current Item: " + viewPager2!!.getCurrentItem())
        viewPager2!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    viewPager2!!.setCurrentItem(0)
                    editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()
                    tvTitle!!.text = resources.getString(R.string.route_list)
                    RouteFragment.getAllRouteList(userId!!, RouteFragment.mListener!!)
                } else if (position == 1) {
                    viewPager2!!.setCurrentItem(1)
                    editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 1)
                    editor!!.commit()
                    tvTitle!!.text = resources.getString(R.string.shop_list)
                    ShopListFragment.getShopList(userId!!)
                }
            }
        })

        if (prefs!!.getString(Api.USER_TYPE, "").equals("TO", true)){
            spinnerLayoutSO2.visibility = View.VISIBLE
            getSOList()
        }else{
            spinnerLayoutSO2.visibility = View.GONE
            progressBar.visibility = View.GONE
        }
        spinnerSO2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinnerSO2.adapter.count >0) {
                    selected_so = p2
                    userId = soList[p2].id
                    srCode = soList[p2].employeeId
                    if (prefs!!.getInt(Api.ROUTE_PAGE_SELECTED, 0) == 0){
                        RouteFragment.getAllRouteList(userId!!, RouteFragment.mListener!!)
                    }else if (prefs!!.getInt(Api.ROUTE_PAGE_SELECTED, 0) == 1){
                        ShopListFragment.getShopList(userId!!)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

    }
    private fun getSOList() {
        ApiServices.apiGET(
            Api.get_all_so_list,
            mQueue!!, token!!, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    viewSOList(response)
                    progressBar.visibility = View.GONE
                    //setDateFilter()
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                    TODO("Not yet implemented")
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    TODO("Not yet implemented")
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                    progressBar.visibility = View.GONE
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }

            })
    }
    private fun viewSOList(response: String) {
        try {
            if (response != null){
                soList.clear()
                val obj = JSONObject(response)
                val toArray = obj.getJSONArray("so_list")
                val soArray = toArray.getJSONObject(0).getJSONArray("sales_officers")
                val soNameList: ArrayList<String> = ArrayList()
                if (soArray.length() >0){
                    for (i in 0 until soArray.length()) {
                        val soObj = soArray.getJSONObject(i)
                        var imageUrl = "null"
                        if (soObj.has("images") && !soObj.isNull("images")){
                            val imageArray = soObj.getJSONArray("images")
                            if (imageArray.length() > 0){
                                val imageobj = imageArray.getJSONObject(0)
                                if (imageobj.has("image_url")){
                                    imageUrl = imageobj.getString("image_url")
                                }
                            }
                        }
                        soList.add(
                            SOList(
                                soObj.getString("id"),
                                soObj.getString("user_name"),
                                soObj.getString("designation"),
                                if(soObj.has("employee_id")) soObj.getString("employee_id") else "",
                                imageUrl
                            )
                        )
                        soNameList.add(soObj.getString("user_name"))

                    }
                }
                val adapter = ArrayAdapter(
                    applicationContext,
                    android.R.layout.simple_spinner_item, soNameList
                )
                spinnerSO2.adapter = adapter
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}