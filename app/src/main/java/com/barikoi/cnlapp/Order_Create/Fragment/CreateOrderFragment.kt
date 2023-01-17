package com.barikoi.cnlapp.Order_Create.Fragment

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_create_order.*
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class CreateOrderFragment : Fragment(){
    lateinit var ACTIVITY: MainActivity
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var viewpagertab: TabLayout? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String ? = ""
    var userId: String ? = ""
    var srId: String ? = ""
    var routeId: String ? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ACTIVITY = context as MainActivity
        //setCurrentFragment(ShopSelectFragment(), ACTIVITY)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkforAttendanceToday()
        setTabViewPager()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_order, container, false)
        viewpagertab = view.findViewById(R.id.viewpagertabOrder)
        viewPager = view.findViewById(R.id.viewPager3)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        editor = prefs!!.edit()
        mContext = context
        mQueue = RequestQueueSingleton.getInstance(context).requestQueue
        token = prefs!!.getString(Api.TOKEN, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        srId = prefs!!.getString(Api.EMPLOYEE_ID, "")
        routeId = prefs!!.getString(Api.SELECTED_ROUTE_ID, "")

        ACTIVITY = context as MainActivity
    }

    /*fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }*/

    fun setTabViewPager() {
        val titles = arrayOf(resources.getString(R.string.select_dokan), resources.getString(R.string.confirm_order))
        val fragments = ArrayList<Fragment>()
        fragments.add(SelectDokanFragment())
        fragments.add(ConfirmOrderFragment())
        viewPager!!.setAdapter(ViewPagerAdapter(parentFragmentManager, lifecycle, fragments))
        // attaching tab mediator
        TabLayoutMediator(viewpagertab!!, viewPager!!,
            TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                tab.text = titles[position]
                //tab.icon = resources.getDrawable(R.drawable.ic_dot)
                if (position == 1){
                    tabBadge = tab
                }
            }).attach()
        viewPager!!.setCurrentItem(0)

        viewPager!!.setUserInputEnabled(false)
        viewpagertab!!.isInlineLabel = true
        /*Log.d("Fragment", "viewpager tab: ${viewpagertab!!.getTabAt(1)!!}")
        if (viewpagertab!!.getTabAt(1)!!.badge != null){
            Log.d("Fragment", "viewpager tab 2: ${viewpagertab!!.getTabAt(1)!!.badge}")
            viewpagertab!!.getTabAt(1)!!.badge!!.number = 3
        }*/
        for (i in 0 until viewpagertab!!.getTabCount()) {
            val tab = (viewpagertab!!.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
            /*if (i == 1) {
                val badge = BadgeView(mContext, tab)
                badge.setText("3")
                badge.show()
            }*/
        }
        Log.d("Fragment", "viewpager current Item: " + viewPager!!.getCurrentItem())
        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    viewPager!!.setCurrentItem(0)
                    setCurrentFragment(SelectDokanFragment(), ACTIVITY)
                } else if (position == 1) {
                    viewPager!!.setCurrentItem(1)
                    ConfirmOrderFragment.checkforOrders(mQueue!!, token!!, userId!!, routeId!!)
                    /*Log.d("Fragment", "viewpager tab 3: ${viewpagertab!!.getTabAt(1)!!.badge}")
                    if (viewpagertab!!.getTabAt(1)!!.badge != null){
                        viewpagertab!!.getTabAt(1)!!.badge!!.number = 3
                    }*/
                }
            }
        })
    }


    companion object{
        var viewPager: ViewPager2? = null
        var tabBadge: TabLayout.Tab? = null
        fun startFragmentWithValue(
            key: String,
            value: Serializable,
            fragmentName: Fragment,
            activity: Activity
        ) {
            try{
                viewPager!!.setCurrentItem(0)
                val bundle = Bundle()
                bundle.putString("from", key)
                bundle.putSerializable(key, value) // Put anything what you want
                fragmentName.arguments = bundle
                val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentLayout2, fragmentName)
                    .addToBackStack(fragmentName.toString())
                    .commit()
            }catch (e:Exception){
                e.printStackTrace()
                Sentry.captureException(e)
            }

        }

        fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
            try{
                //viewPager!!.setCurrentItem(0)
                val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentLayout2, fragment!!)
                fragmentTransaction.commit()
                //fragmentManager.executePendingTransactions()
            }catch (e:Exception){
                e.printStackTrace()
                Sentry.captureException(e)
            }

        }
    }

    private fun checkforAttendanceToday() {
        val today = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        ApiServices.apiGET(
            Api.get_attendance+"?start_date="+df.format(today)+"&end_date="+df.format(today),
            mQueue!!, token!!, object : ApiServiceListener {
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


}