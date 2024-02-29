package com.barikoi.cnlapp.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.Attendance.AttendanceFragment
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.Chat.Fragment.ChatFragment
import com.barikoi.cnlapp.Fragment.MapFragment
import com.barikoi.cnlapp.Notice.NoticeActivity
import com.barikoi.cnlapp.OrderSummary.SO.OrderSummaryActivity
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.Order_Create.Fragment.CreateOrderFragment
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.ProductStock.ProductSummaryActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Fragment.SO.HomeFragment
import com.barikoi.cnlapp.StatisticsHome.Fragment.TO.HomeTOFragment
import com.barikoi.cnlapp.TradeOffers.TradeOffersActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.VisitReport.VisitReportActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.data.remote.models.trace.request.TraceLognRequest
import com.barikoi.cnlapp.databinding.ActivityMainBinding
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.extension.formatDate
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.android.synthetic.main.app_content_main.*
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private var drawer: DrawerLayout? = null
    private lateinit var navigationDrawer: NavigationView
    private lateinit var menuDrawer: ImageView
    private lateinit var tvHeaderUserName: TextView
    private var token: String? = ""
    private var userId: String? = ""
    private var userType: String? = ""
    private lateinit var navView: BottomNavigationView
    var queue: RequestQueue? = null

    companion object {
        var routeName_selected: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        queue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        token = sharePrefUtils.getString(Api.TOKEN)
        userId = sharePrefUtils.getString(Api.USER_ID)
        userType = sharePrefUtils.getString(Api.USER_TYPE)
        navView = findViewById(R.id.bottom_nav_view)

        navView.background = null
        navView.menu.getItem(2).isEnabled = false
        navView.menu.getItem(2).isVisible = false

        navigationDrawer = findViewById(R.id.nav_view)
        navigationDrawer.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout)

        routeName_selected = findViewById(R.id.routeNameSelected)

        menuDrawer = findViewById(R.id.drawer)
        menuDrawer!!.setOnClickListener {
            drawer!!.openDrawer(
                GravityCompat.START, true
            )
        }

        checkAttendance()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val startDate = calendar.time.formatDate()
        val endDate = Calendar.getInstance().time.formatDate()


        userLayout.visibility = View.VISIBLE
        tvUserName.text = sharePrefUtils.getString(Api.NAME)

        if (sharePrefUtils.getString(Api.TRACE_TOKEN).isNullOrEmpty() || sharePrefUtils.getString(
                Api.TRACE_LOGIN_LAST
            ) != Calendar.getInstance().time.formatDate()
        ) {
            viewModel.traceLogin(TraceLognRequest(BuildConfig.TRACE_EMAIL, BuildConfig.TRACE_PASS))
        }
        viewModel.traceLogin(TraceLognRequest(BuildConfig.TRACE_EMAIL, BuildConfig.TRACE_PASS))


        getAuthUser(
            token,
            Api.authUserCheck + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59&app_version=" + BuildConfig.VERSION_NAME
        )
        if (userType.equals("TO", true)) {
            binding.mainAppContent.routeNameSelected.visibility = View.GONE
            setCurrentFragment(HomeTOFragment(), this@MainActivity)
        } else {
            setCurrentFragment(HomeFragment(), this@MainActivity)
        }

        val header = navigationDrawer.getHeaderView(0)
        tvHeaderUserName = header.findViewById(R.id.textView_username)
        val tvHeaderEmail = header.findViewById<TextView>(R.id.textView_useremail)
        val btnLogout = findViewById<AppCompatButton>(R.id.btnLogout)
        val tvAppVersion = header.findViewById<TextView>(R.id.textView_version)
        tvAppVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        tvHeaderUserName.text = sharePrefUtils.getString(Api.NAME)

        if (sharePrefUtils.getString(Api.EMAIL)!!.isNotEmpty()) {
            tvHeaderEmail.visibility = View.VISIBLE
            tvHeaderEmail.text = sharePrefUtils.getString(Api.EMAIL)
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this@MainActivity, R.style.AlertDialog).setTitle(R.string.logout)
                .setMessage(R.string.sure_log_out).setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    logout(this@MainActivity)
                }.setNegativeButton(
                    R.string.cancel
                ) { _, _ -> }
                .setIcon(ResourcesCompat.getDrawable(resources, R.drawable.ic_warning, null)).show()
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true)) {
            fab_order.visibility = View.GONE
        } else {
            fab_order.visibility = View.VISIBLE
        }

        fab_order.setOnClickListener {
            fab_order.background.setTint(resources.getColor(R.color.cnl_color_2))
            fab_order.drawable.setTint(resources.getColor(R.color.white))
            navView.selectedItemId = R.id.navigation_order
            userLayout.visibility = View.GONE
            tvTitle.text = resources.getString(R.string.order_collection)
            tvTitle.visibility = View.VISIBLE
            setCurrentFragment(CreateOrderFragment(), this@MainActivity)
        }

        navView.setOnItemSelectedListener { item ->
            if (fab_order.isVisible) {
                fab_order.background.setTint(resources.getColor(R.color.white))
                fab_order.drawable.setTint(resources.getColor(R.color.fab_icon))
            }
            when (item.itemId) {
                R.id.navigation_home -> {
                    tvTitle.text = ""
                    tvTitle.visibility = View.GONE
                    userLayout.visibility = View.VISIBLE
                    if (userType.equals("TO", true)) {
                        setCurrentFragment(HomeTOFragment(), this@MainActivity)
                    } else {
                        setCurrentFragment(HomeFragment(), this@MainActivity)
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_route -> {
                    tvTitle!!.text = resources.getString(R.string.route_plan)
                    userLayout.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    setCurrentFragment(MapFragment(), this@MainActivity)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_chat -> {
                    tvTitle!!.text = resources.getString(R.string.title_chat)
                    setCurrentFragment(ChatFragment(), this@MainActivity)
                    userLayout.visibility = View.GONE
                    tvTitle.visibility = View.VISIBLE
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_attendance -> {
                    tvTitle.text = resources.getString(R.string.attendance)
                    tvTitle.visibility = View.VISIBLE
                    userLayout.visibility = View.GONE
                    setCurrentFragment(AttendanceFragment(), this@MainActivity)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        traceLoginObserve()
    }

    private fun traceLoginObserve() {
        lifecycleScope.launch {
            viewModel.loginResponse.observe(this@MainActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("traceLoginObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("traceLoginObserve: Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("traceLoginObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("traceLoginObserve::Success ${it.data}")
                        sharePrefUtils.saveString(Api.TRACE_TOKEN, it.data?.token!!)
                        sharePrefUtils.saveString(
                            Api.TRACE_LOGIN_LAST, Calendar.getInstance().time.formatDate()
                        )
                    }
                }
            }
        }
    }

    private fun traceLogin() {
        val parameters: HashMap<String, String> = HashMap()
        parameters["email"] = "miraz@barikoi.xyz"
        parameters["password"] = "m12345678M"

        ApiServices.apiPOSTNew(Api.traceLogin,
            queue!!,
            "",
            parameters,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val data = JSONObject(response)
                        if (data.has("data") && !data.isNull("data")) {
                            sharePrefUtils.saveString(Api.TRACE_TOKEN, data.getString("data"))
                            traceAuthCheck(data.getString("data"))
                        }
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    }
                }

                override fun onJSONResponseSuccess(response: JSONObject) {
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {

                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
//                    traceLogin()
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun traceAuthCheck(tokenT: String) {
        ApiServices.apiGET(Api.traceAuthCheck, queue!!, tokenT, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    val obj = JSONObject(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {
                TODO("Not yet implemented")
            }

            override fun onNetworkResponseSuccess(response: NetworkResponse) {
                TODO("Not yet implemented")
            }

            override fun onResponseFailure(error: VolleyError) {/*ViewUtils.getErrorResponse(error, applicationContext)*/
//                traceLogin()
            }

            override fun onException(e: Exception) {/*Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()*/
//                traceLogin()
            }

        })

    }

    private fun getAuthUser(token: String?, url: String) {
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    val obj = JSONObject(response)
                    if (obj.has("user")) {
                        val userObj = obj.getJSONObject("user")
                        if (userObj.has("so_ranking") && !userObj.isNull("so_ranking")) {
                            rankLayout.visibility = View.VISIBLE
                            tvRank.setText(userObj.getInt("so_ranking").toString())
                            rank_suffix.setText(toOrdinal(userObj.getInt("so_ranking")))
                        } else {
                            //rankLayout.visibility = View.VISIBLE
                            //tvRank.setText("0")
                        }
                        //routeName_selected!!.setText(prefs!!.getString(Api.SELECTED_ROUTE_NAME, ""))
                    }
                } catch (e: Exception) {
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
                if (error is TimeoutError) {
                    //mListerner.onFailure("Request timeout!! Check your internet connection or Contact Admin")
                    Toast.makeText(
                        applicationContext,
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    //mListerner.onFailure("Turn on your internet connection and Try again")
                    Toast.makeText(
                        applicationContext,
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is AuthFailureError) {
                    logout(applicationContext)
                }
                if (error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        //Toast.makeText(mContext.getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        //mListerner.onFailure(data.getString("message"))
                        Toast.makeText(
                            applicationContext, data.getString("message"), Toast.LENGTH_LONG
                        ).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        //mListerner.onFailure(e.message)
                        Sentry.captureException(e)
                        Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }

            override fun onException(e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAttendance() {
        ApiServices.apiGET(Api.check_today_attendance,
            queue!!,
            token!!,
            object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    try {
                        val data = JSONObject(response)
                        if (data.has("attendances") && !data.isNull("attendances")) {
                            val attendanceObj = data.getJSONObject("attendances")
                            val checkIn = attendanceObj.getString("checkin_time")
                            val checkOut = attendanceObj.getString("checkout_time")
                            if (!checkIn.equals("null") && !checkOut.equals("null")) {
                                BarikoiTrace.stopTracking()
                            } else if (!checkIn.equals("null") && checkOut.equals("null")) {
                                if (!BarikoiTrace.isLocationTracking()) {
                                    AppLogger.log("TRACE START CALL")
                                    ViewUtils.startTracking(this@MainActivity, this@MainActivity)
                                }
                            } else {
                                BarikoiTrace.stopTracking()
                            }
                        } else {
                            BarikoiTrace.stopTracking()
                        }

                    } catch (e: Exception) {
                        Sentry.captureException(e)
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
                    ViewUtils.getErrorResponse(error, applicationContext)
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    fun toOrdinal(day: Int) = if (day % 100 / 10 == 1) "th"
    else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }

    private fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_order_summary) {
            if (userType.equals("TO", true)) {
                startActivity(Intent(this@MainActivity, OrderSummaryTOActivity::class.java))
            } else {
                startActivity(Intent(this@MainActivity, OrderSummaryActivity::class.java))
            }
        } else if (id == R.id.menu_shop_route) {
            startActivity(Intent(this@MainActivity, RouteActivity::class.java))
        } else if (id == R.id.menu_order_delivery_update) {
            startActivity(Intent(this@MainActivity, OrderDeliveryUpdateActivity::class.java))
        } else if (id == R.id.menu_notice) {
            startActivity(Intent(this@MainActivity, NoticeActivity::class.java))
        } else if (id == R.id.menu_visit_report) {
            startActivity(Intent(this@MainActivity, VisitReportActivity::class.java))
        } else if (id == R.id.menu_trade_offers) {
            startActivity(Intent(this@MainActivity, TradeOffersActivity::class.java))
        } else if (id == R.id.menu_product_summary) {
            startActivity(Intent(this@MainActivity, ProductSummaryActivity::class.java))
        } else if (id == R.id.menu_product_stock_update) {
            startActivity(Intent(this@MainActivity, ProductStockUpdateActivity::class.java))
        }/*else if (id == R.id.menu_incentive){

        }*/
        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun logout(context: Context) {
        sharePrefUtils.clear()

        val queue = RequestQueueSingleton.getInstance(context.applicationContext).requestQueue
        val request: StringRequest = object :
            StringRequest(Method.POST, Api.logouturl, Response.Listener { response: String? ->
                val home = Intent(context, SplashActivity::class.java)
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(home)
                finish()

            }, Response.ErrorListener { error: VolleyError? ->
                if (error?.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Verify", "message: $s")
                        var data: JSONObject? = null
                        try {
                            data = JSONObject(s)
                            Toast.makeText(
                                context.applicationContext,
                                "Error: " + data.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()
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
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(home)
                finish()
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

}