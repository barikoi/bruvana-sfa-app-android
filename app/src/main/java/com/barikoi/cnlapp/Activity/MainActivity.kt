package com.barikoi.cnlapp.Activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Attendance.AttendanceFragment
import com.barikoi.cnlapp.Chat.Fragment.ChatFragment
import com.barikoi.cnlapp.Fragment.MapFragment
import com.barikoi.cnlapp.Notice.NoticeActivity
import com.barikoi.cnlapp.Order_Create.Fragment.CreateOrderFragment
import com.barikoi.cnlapp.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.ProductStock.ProductSummaryActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Fragment.HomeFragment
import com.barikoi.cnlapp.TradeOffers.TradeOffersActivity
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.Utils.ApiService.ApiServices
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.barikoi.cnlapp.Utils.ViewUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_order_summary.*
import kotlinx.android.synthetic.main.appcontent_main.*
import kotlinx.android.synthetic.main.appcontent_main.tvTitle
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawer: DrawerLayout? = null
    private var navigationDrawer: NavigationView? = null
    private var menu_drawer: ImageView? = null
    private var tvHeaderUserName: TextView? = null
    private var token : String?= ""
    private var userId: String? = ""
    private var userName: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var nav_view: BottomNavigationView? = null
    var queue : RequestQueue? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()
        queue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        token = prefs!!.getString(Api.TOKEN, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        userName = prefs!!.getString(Api.NAME, "")
        //val route = prefs!!.getString(Api.SELECTED_ROUTE_NAME, "")
        nav_view = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        nav_view!!.background = null
        nav_view!!.menu.getItem(2).isEnabled = false
        nav_view!!.menu.getItem(2).isVisible = false

        navigationDrawer = findViewById(R.id.nav_view)
        navigationDrawer!!.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout)

        menu_drawer = findViewById<ImageView>(R.id.drawer)
        menu_drawer!!.setOnClickListener(View.OnClickListener {
            drawer!!.openDrawer(
                GravityCompat.START,
                true
            )
        })

        val c = Calendar.getInstance()
        //c.add(Calendar.DAY_OF_WEEK, -7)
        c.set(Calendar.DAY_OF_MONTH, 1);
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val StartDate = df.format(start)
        val EndDate = df.format(end)

        userLayout.visibility = View.VISIBLE
        tvUserName.setText(userName)
        getAuthUser(token, Api.authUserCheck+"?start_date=2022-04-30 00:00:00"/*+StartDate*/+"&end_date="+EndDate)

        setCurrentFragment(HomeFragment(), this@MainActivity)

        val header = navigationDrawer!!.getHeaderView(0)
        tvHeaderUserName = header.findViewById<TextView>(R.id.textView_username)
        val tvHeaderEmail = header.findViewById<TextView>(R.id.textView_useremail)
        val btnLogout = findViewById<AppCompatButton>(R.id.btnLogout)
        tvHeaderUserName!!.text = userName
        if (prefs!!.getString(Api.EMAIL, "")!!.length >0){
            tvHeaderEmail.visibility = View.VISIBLE
            tvHeaderEmail.text = prefs!!.getString(Api.EMAIL, "")
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
                .setTitle(R.string.logout)
                .setMessage(R.string.sure_log_out)
                .setPositiveButton(android.R.string.yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        logout(this@MainActivity)
                    })
                .setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> }) // do nothing
                .setIcon(resources.getDrawable(R.drawable.warning))
                .show()
        }

        //setCurrentFragment(HomeFragment(), this@MainActivity)
        fab_order.setOnClickListener {
            fab_order.background.setTint(resources.getColor(R.color.cnl_color_2))
            fab_order.drawable.setTint(resources.getColor(R.color.white))
            nav_view!!.selectedItemId = R.id.navigation_order
            userLayout.visibility = View.GONE
            tvTitle.text = resources.getString(R.string.order_collection)
            tvTitle.visibility = View.VISIBLE
            setCurrentFragment(CreateOrderFragment(), this@MainActivity)
        }

        nav_view!!.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            fab_order.background.setTint(resources.getColor(R.color.white))
            fab_order.drawable.setTint(resources.getColor(R.color.fab_icon))
            when (item.itemId) {
                R.id.navigation_home -> {
                    tvTitle.text = ""
                    tvTitle.visibility = View.GONE
                    userLayout.visibility = View.VISIBLE
                    setCurrentFragment(HomeFragment(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_route -> {
                    tvTitle!!.text = resources.getString(R.string.route_plan)
                    userLayout.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    setCurrentFragment(MapFragment(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                /*R.id.navigation_order -> {
                    userLayout.visibility = View.GONE
                    tvTitle.text = ""
                    tvTitle.visibility = View.VISIBLE
                    return@OnNavigationItemSelectedListener true
                }*/
                R.id.navigation_chat -> {
                    tvTitle!!.text = resources.getString(R.string.title_chat)
                    setCurrentFragment(ChatFragment(), this@MainActivity)
                    userLayout.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_attendance -> {
                    tvTitle.text = resources.getString(R.string.attendance)
                    tvTitle.visibility = View.VISIBLE
                    userLayout.visibility = View.GONE
                    setCurrentFragment(AttendanceFragment(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

    }

    private fun getAuthUser(token: String?, url: String) {
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener{
            override fun onResponseSuccess(response: String) {
                if (response != null){
                    try {
                        val obj = JSONObject(response)
                        if (obj.has("user")){
                            val userObj = obj.getJSONObject("user")
                            if (userObj.has("so_ranking") && !userObj.isNull("so_ranking")){
                                rankLayout.visibility = View.VISIBLE
                                tvRank.setText(userObj.getInt("so_ranking").toString())
                                rank_suffix.setText(toOrdinal(userObj.getInt("so_ranking")))
                            }else{
                                //rankLayout.visibility = View.VISIBLE
                                //tvRank.setText("0")
                            }
                            routeNameSelected.setText(prefs!!.getString(Api.SELECTED_ROUTE_NAME, ""))
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {
                ViewUtils.getErrorResponse(error, applicationContext)
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }

        })

    }
    fun toOrdinal(day: Int) =
            if (day % 100 / 10 == 1) "th"
            else when (day % 10) { 1 -> "st" 2 -> "nd" 3 -> "rd" else -> "th" }

    fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_order_summary) {
            startActivity(Intent(this@MainActivity, OrderSummaryActivity::class.java))
        } else if (id == R.id.menu_shop_route) {
            startActivity(Intent(this@MainActivity, RouteActivity::class.java))
        } else if (id == R.id.menu_notice){
            startActivity(Intent(this@MainActivity, NoticeActivity::class.java))
        }else if (id == R.id.menu_trade_offers){
           startActivity(Intent(this@MainActivity, TradeOffersActivity::class.java))
        }else if (id == R.id.menu_product_summary){
            startActivity(Intent(this@MainActivity, ProductSummaryActivity::class.java))
        } else if (id == R.id.menu_product_stock_update){
            startActivity(Intent(this@MainActivity, ProductStockUpdateActivity::class.java))
        }/*else if (id == R.id.menu_incentive){

        }*/
        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun logout(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val token = prefs.getString(Api.TOKEN, "")
        val editor = prefs.edit()
        editor.remove(Api.TOKEN)
        editor.remove(Api.NAME)
        editor.remove(Api.USER_ID)
        editor.remove(Api.PHONE)
        editor.commit()

        /*val home = Intent(context, SplashActivity::class.java)
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(home)*/
        val queue = RequestQueueSingleton.getInstance(context.applicationContext).requestQueue
        val request: StringRequest = object : StringRequest(
            Method.GET,
            Api.logouturl,
            Response.Listener { response: String? ->
                val home = Intent(context, SplashActivity::class.java)
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(home)

            },
            Response.ErrorListener { error: VolleyError? ->
                if (error != null && error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Verify", "message: \$s")
                        var data: JSONObject? = null
                        try {
                            data = JSONObject(s)
                            Toast.makeText(
                                context.applicationContext,
                                "Error: "+data.getString("error"),
                                Toast.LENGTH_SHORT
                            ).show()
                            /*handleResponse(
                                error,
                                context.applicationContext
                            )*/
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Sentry.captureException(e)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Sentry.captureException(e)
                    }
                }
                val home = Intent(context, SplashActivity::class.java)
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(home)
            }) {
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Accept"] = "application/json"
                if (token != "") {
                    params["Authorization"] = "bearer $token"
                }
                return params
            }
        }
        queue.add(request)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }

}