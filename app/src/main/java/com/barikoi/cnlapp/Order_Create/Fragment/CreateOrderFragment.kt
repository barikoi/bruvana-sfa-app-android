package com.barikoi.cnlapp.Order_Create.Fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.barikoi.cnlapp.Activity.MainActivity
import com.barikoi.cnlapp.Adapter.ViewPagerAdapter
import com.barikoi.cnlapp.Order_Create.Fragment.ConfirmOrderFragment.Companion.mCallback
import com.barikoi.cnlapp.Order_Create.Fragment.CreateOrderFragment.Companion.viewPager
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.barikoi.cnlapp.callback.OnBackPressedListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.sentry.Sentry
import kotlinx.android.synthetic.main.fragment_create_order.*
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateOrderFragment : Fragment(){
    lateinit var ACTIVITY: MainActivity
    var viewpagertab: TabLayout? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    var mContext: Context? = null
    var mQueue: RequestQueue? = null
    var token: String ? = ""
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
        viewpagertab = view.findViewById(R.id.viewpagertab2)
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
        srId = prefs!!.getString(Api.SR_CODE, "")
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
            }).attach()
        viewPager!!.setCurrentItem(0)

        viewPager!!.setUserInputEnabled(false)

        for (i in 0 until viewpagertab!!.getTabCount()) {
            val tab = (viewpagertab!!.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(12, 12, 8, 12)
            tab.requestLayout()
        }
        Log.d("Fragment", "viewpager current Item: " + viewPager!!.getCurrentItem())
        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("Fragment", "viewpager tab pos: $position")
                if (position == 0) {
                    viewPager!!.setCurrentItem(0)
                    setCurrentFragment(SelectDokanFragment(), ACTIVITY)
                    /*editor!!.putInt(Api.ROUTE_PAGE_SELECTED, 0)
                    editor!!.commit()*/
                } else if (position == 1) {
                    //viewPager!!.setCurrentItem(1)
                    //setCurrentFragment(ConfirmOrderFragment(), ACTIVITY)
                    ConfirmOrderFragment.checkforOrders(mQueue!!, token!!, srId!!, routeId!!)
                }
            }
        })
    }


    companion object{
        var viewPager: ViewPager2? = null
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
                    .add(R.id.fragmentLayout2, fragmentName)
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