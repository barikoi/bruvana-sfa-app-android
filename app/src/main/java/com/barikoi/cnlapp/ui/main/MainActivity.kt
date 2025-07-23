package com.barikoi.cnlapp.ui.main

import DefaultLocaleHelper
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.ui.route.RouteActivity
import com.barikoi.cnlapp.Attendance.AttendanceFragment
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.Chat.Fragment.ChatFragment
import com.barikoi.cnlapp.ui.shop_map.MapFragment
import com.barikoi.cnlapp.Notice.NoticeActivity
import com.barikoi.cnlapp.OrderSummary.SO.OrderSummaryActivity
import com.barikoi.cnlapp.OrderSummary.TO.OrderSummaryTOActivity
import com.barikoi.cnlapp.Order_Delivery.OrderDeliveryUpdateActivity
import com.barikoi.cnlapp.ui.ProductStock.ProductStockUpdateActivity
import com.barikoi.cnlapp.ui.product_summary.ProductSummaryActivity
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.StatisticsHome.Fragment.SO.HomeFragment
import com.barikoi.cnlapp.ui.trade_offers.TradeOffersActivity
import com.barikoi.cnlapp.ui.visit_report.VisitReportActivity
import com.barikoi.cnlapp.ui.approval.StockRequestApprovalActivity
import com.barikoi.cnlapp.base.ac.BaseActivity
import com.barikoi.cnlapp.base.api.ApiState
import com.barikoi.cnlapp.base.api.NetworkFailureMessage
import com.barikoi.cnlapp.databinding.ActivityMainBinding
import com.barikoi.cnlapp.ui.notification.NotificationActivity
import com.barikoi.cnlapp.ui.create_order.select_shop.SelectShopFragment
import com.barikoi.cnlapp.ui.request.StockRequestActivity
import com.barikoi.cnlapp.ui.auth.LoginActivity
import com.barikoi.cnlapp.ui.gift_summary.GiftSummaryActivity
import com.barikoi.cnlapp.ui.main.vm.MainViewModel
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.ApiService.ApiServiceListener
import com.barikoi.cnlapp.utils.ApiService.ApiServices
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.RequestQueueSingleton
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.barikoi.cnlapp.utils.SwitchMultiButton
import com.barikoi.cnlapp.utils.ViewUtils
import com.barikoi.cnlapp.utils.extension.loadingDialog
import com.barikoi.cnlapp.utils.extension.setHapticClickListener
import com.barikoi.cnlapp.utils.extension.toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.onesignal.OneSignal
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import androidx.core.view.get


@AndroidEntryPoint
class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    @Inject
    lateinit var networkFailureMessage: NetworkFailureMessage

    private lateinit var drawer: DrawerLayout

    private var navigationDrawer: NavigationView? = null
    private lateinit var menuDrawer: ImageView
    private var tvHeaderUserName: TextView? = null
    private var token: String? = ""
    private var userId: String? = ""
    private var userType: String? = ""
    private var userName: String? = ""

    private lateinit var navView: BottomNavigationView
    var queue: RequestQueue? = null

    private lateinit var logoutDialog: Dialog


    private lateinit var tvApprovalCount: TextView

    companion object {
        @SuppressLint("StaticFieldLeak")
        var routeName_selected: TextView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog {
            logoutDialog = it
        }

        startTraceLoginObserve()
        startTraceAuthUserObserve()

        queue = RequestQueueSingleton.getInstance(applicationContext).requestQueue

        token = sharePrefUtils.getString(Api.TOKEN)
        userId = sharePrefUtils.getString(Api.USER_ID)
        userType = sharePrefUtils.getString(Api.USER_TYPE)
        userName = sharePrefUtils.getString(Api.NAME)

        navView = findViewById(R.id.bottom_nav_view)

        navView.background = null
        navView.menu[2].isEnabled = false
        navView.menu[2].isVisible = false

        navigationDrawer = findViewById(R.id.nav_view)
        navigationDrawer!!.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout)

        routeName_selected = findViewById(R.id.routeNameSelected)

        menuDrawer = findViewById(R.id.drawer)
        menuDrawer.setOnClickListener {
            drawer.openDrawer(
                GravityCompat.START,
                true
            )
        }

        binding.appContentMain.ivNotification.setHapticClickListener {
            startActivity(Intent(this@MainActivity, NotificationActivity::class.java))
        }

        binding.appContentMain.tvNotificationCount.setHapticClickListener {
            startActivity(Intent(this@MainActivity, NotificationActivity::class.java))
        }

        binding.appContentMain.tvNotificationCount.text = "0"

        startApprovalCountObserve()
        startLogoutObserve()
        checkAttendance()

        val c = Calendar.getInstance()
        c.set(Calendar.DAY_OF_MONTH, 1)
        val end = Calendar.getInstance().time
        val start = c.time
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val startDate = df.format(start)
        val endDate = df.format(end)

        binding.appContentMain.userLayout.visibility = View.VISIBLE
        binding.appContentMain.tvUserName.text = userName

        if (sharePrefUtils.getString(Api.TRACE_TOKEN).isNullOrEmpty()) {
            viewModel.traceLogin(BuildConfig.TRACE_USER, BuildConfig.TRACE_PASS)
        } else {
            viewModel.traceAuthUser()
        }

        getAuthUser(
            token,
            Api.authUserCheck + "?start_date=" + startDate + " 00:00:00" + "&end_date=" + endDate + " 23:59:59&app_version=" + BuildConfig.VERSION_NAME
        )

        if (userType.equals("TO", true) || userType.equals("ASM", true)) {
            binding.appContentMain.routeNameSelected.visibility = View.GONE
            setCurrentFragment(
                com.barikoi.cnlapp.ui.home.HomeFragment(
                    sharePrefUtils.getString(Api.USER_TYPE)!!
                ), this@MainActivity
            )
        } else {
            setCurrentFragment(HomeFragment(), this@MainActivity)
        }


        val header = navigationDrawer!!.getHeaderView(0)
        tvHeaderUserName = header.findViewById(R.id.textView_username)
        val tvHeaderEmail = header.findViewById<TextView>(R.id.textView_user_email)
        val btnLogout = findViewById<AppCompatButton>(R.id.btnLogout)
        val tvAppVersion = header.findViewById<TextView>(R.id.textView_version)
        val versionName: String = BuildConfig.VERSION_NAME
        tvAppVersion.text = getString(R.string.version, versionName)
        tvHeaderUserName!!.text = userName
        if (sharePrefUtils.getString(Api.EMAIL)!!
                .isNotEmpty() && !sharePrefUtils.getString(Api.EMAIL)!!
                .equals("null")
        ) {
            tvHeaderEmail.visibility = View.VISIBLE
            tvHeaderEmail.text = sharePrefUtils.getString(Api.EMAIL)
        }

        val switchLanguage = navigationDrawer!!.menu.findItem(R.id.menu_language).actionView!!
            .findViewById<SwitchMultiButton>(R.id.switchLanguage)

        if (androidx.compose.ui.text.intl.Locale.current.language == "en") {
            switchLanguage.setSelectedTab(0)
        } else {
            switchLanguage.setSelectedTab(1)
        }

        switchLanguage.setOnSwitchListener { _, tabText ->
            AppLogger.log("Locale: $tabText")
            if (tabText == "ENG") {
                DefaultLocaleHelper.getInstance(this).setCurrentLocale("en")
            } else {
                DefaultLocaleHelper.getInstance(this).setCurrentLocale("bn")
            }

            if (navView.selectedItemId != R.id.navigation_order) {
                recreate()
            } else {
                lifecycleScope.launch {
                    delay(100)
                    restartApp(this@MainActivity)
                }
            }
        }

        btnLogout.setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
            AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
                .setTitle(R.string.logout)
                .setMessage(R.string.sure_log_out)
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setIcon(AppCompatResources.getDrawable(this, R.drawable.warning))
                .show()
        }

        if (sharePrefUtils.getString(Api.USER_TYPE).equals("TO", true) ||
            sharePrefUtils.getString(Api.USER_TYPE).equals("ASM")
        ) {
            binding.appContentMain.fabOrder.visibility = View.GONE
        } else {
            binding.appContentMain.fabOrder.visibility = View.VISIBLE
        }

        binding.appContentMain.fabOrder.setOnClickListener {
            binding.appContentMain.fabOrder.background.setTint(
                ContextCompat.getColor(this, R.color.cnl_color_2)
            )
            binding.appContentMain.fabOrder.drawable.setTint(
                ContextCompat.getColor(this, R.color.white)
            )
            navView.selectedItemId = R.id.navigation_order
            binding.appContentMain.userLayout.visibility = View.GONE
            binding.appContentMain.tvTitle.text = resources.getString(R.string.order_collection)
            binding.appContentMain.tvTitle.visibility = View.VISIBLE
            setCurrentFragment(SelectShopFragment(), this@MainActivity)
        }

        tvApprovalCount =
            navigationDrawer!!.menu.findItem(R.id.menu_request_approval).actionView!!.findViewById(R.id.tvApprovalCount)

        navigationDrawer!!.menu.findItem(R.id.menu_request_approval).isVisible =
            sharePrefUtils.getString(Api.USER_TYPE) == "TO"


        navigationDrawer!!.menu.findItem(R.id.menu_visit_report).isVisible =
            sharePrefUtils.getString(Api.USER_TYPE) != "ASM"

        navigationDrawer!!.menu.findItem(R.id.menu_product_stock_request).isVisible =
            sharePrefUtils.getString(Api.USER_TYPE) == "SO"

        navView.setOnItemSelectedListener { item ->
            if (binding.appContentMain.fabOrder.isVisible) {
                binding.appContentMain.fabOrder.background.setTint(
                    ContextCompat.getColor(this, R.color.white)
                )
                binding.appContentMain.fabOrder.drawable.setTint(
                    ContextCompat.getColor(this, R.color.cnl_color_2)
                )
            }
            when (item.itemId) {
                R.id.navigation_home -> {
                    binding.appContentMain.tvTitle.text = ""
                    binding.appContentMain.tvTitle.visibility = View.GONE
                    binding.appContentMain.userLayout.visibility = View.VISIBLE
                    if (userType.equals("TO", true) || userType.equals("ASM", true)) {
                        setCurrentFragment(
                            com.barikoi.cnlapp.ui.home.HomeFragment(
                                sharePrefUtils.getString(
                                    Api.USER_TYPE
                                )!!
                            ), this@MainActivity
                        )
                    } else {
                        setCurrentFragment(HomeFragment(), this@MainActivity)
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_route -> {
                    binding.appContentMain.tvTitle.text = resources.getString(R.string.route_plan)
                    binding.appContentMain.userLayout.visibility = View.GONE
                    binding.appContentMain.tvTitle.visibility = View.VISIBLE
                    setCurrentFragment(MapFragment(), this@MainActivity)
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_chat -> {
                    binding.appContentMain.tvTitle.text = resources.getString(R.string.title_chat)
                    setCurrentFragment(ChatFragment(), this@MainActivity)
                    binding.appContentMain.userLayout.visibility = View.GONE
                    binding.appContentMain.tvTitle.visibility = View.VISIBLE
                    return@setOnItemSelectedListener true
                }

                R.id.navigation_attendance -> {
                    binding.appContentMain.tvTitle.text = resources.getString(R.string.attendance)
                    binding.appContentMain.tvTitle.visibility = View.VISIBLE
                    binding.appContentMain.userLayout.visibility = View.GONE
                    setCurrentFragment(AttendanceFragment(), this@MainActivity)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

    }

    private fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()  // Finish the current activity if context is an activity
        Runtime.getRuntime().exit(0)  // Kill the process to ensure a full restart
    }

    private fun startTraceLoginObserve() {
        lifecycleScope.launch {
            viewModel.traceLoginResponse.observe(this@MainActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startTraceLoginObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startTraceLoginObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startTraceLoginObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startTraceLoginObserve:: Success ${it.data}")

                        if (it.data != null) {
                            sharePrefUtils.saveString(Api.TRACE_TOKEN, it.data.token)

                            viewModel.traceAuthUser()
                        }
                    }
                }
            }
        }
    }

    private fun startTraceAuthUserObserve() {
        lifecycleScope.launch {
            viewModel.traceAuthUserResponse.observe(this@MainActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startTraceAuthUserObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startTraceAuthUserObserve::Error ${it.error}")
                        toast(networkFailureMessage.handleFailure(it.error!!))

                        viewModel.traceLogin(BuildConfig.TRACE_USER, BuildConfig.TRACE_PASS)
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startTraceAuthUserObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startTraceAuthUserObserve:: Success ${it.data}")
                    }
                }
            }
        }
    }

    private fun traceLogin() {
        val parameters: MutableMap<String, String> = HashMap()
        parameters["email"] = "carenutrition@gmail.com"
        parameters["password"] = "12345678"

        ApiServices.apiPOST(
            Api.traceLogin,
            queue!!, "", parameters, object : ApiServiceListener {
                override fun onResponseSuccess(response: String) {
                    AppLogger.log("TRACE RESPONSE: $response")
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
                    AppLogger.log("TRACE RESPONSE: $response")
                }

                override fun onNetworkResponseSuccess(response: NetworkResponse) {
                    AppLogger.log("TRACE RESPONSE: $response")
                }

                override fun onResponseFailure(error: VolleyError) {
                    ViewUtils.getErrorResponse(error, applicationContext)
                    traceLogin()
                }

                override fun onException(e: Exception) {
                    Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun traceAuthCheck(tokenT: String) {
        ApiServices.apiGET(Api.traceAuthCheck, queue!!, tokenT, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {}

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

            override fun onResponseFailure(error: VolleyError) {
                traceLogin()
            }

            override fun onException(e: Exception) {
                traceLogin()
            }
        })
    }

    private fun getAuthUser(token: String?, url: String) {
        ApiServices.apiGET(url, queue!!, token!!, object : ApiServiceListener {
            override fun onResponseSuccess(response: String) {
                try {
                    val obj = JSONObject(response)
                    OneSignal.User.addTags(
                        mapOf(
                            "employee_id" to obj.getJSONObject("user").getString("employee_id")
                        )
                    )
                    if (obj.has("user")) {
                        val userObj = obj.getJSONObject("user")
                        if (userObj.has("so_ranking") && !userObj.isNull("so_ranking")) {
                            binding.appContentMain.rankLayout.visibility = View.VISIBLE
                            binding.appContentMain.tvRank.text =
                                userObj.getInt("so_ranking").toString()
                            binding.appContentMain.rankSuffix.text =
                                toOrdinal(userObj.getInt("so_ranking"))
                        } else {
                            toast("User not found")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onJSONResponseSuccess(response: JSONObject) {}

            override fun onNetworkResponseSuccess(response: NetworkResponse) {}

            override fun onResponseFailure(error: VolleyError) {
                if (error is TimeoutError) {
                    Toast.makeText(
                        applicationContext,
                        "Request timeout!! Check your internet connection or Contact Admin",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is NoConnectionError) {
                    Toast.makeText(
                        applicationContext,
                        "Turn on your internet connection and Try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (error is AuthFailureError) {
                    viewModel.logout()
                }
                if (error.networkResponse != null) {
                    try {
                        val s = String(error.networkResponse.data)
                        Log.d("Routes", "message: $s")
                        val data = JSONObject(s)
                        Toast.makeText(
                            applicationContext,
                            data.getString("message"),
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: UnsupportedEncodingException) {
                        Sentry.captureException(e)
                        e.printStackTrace()
                    } catch (e: JSONException) {
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

    override fun onResume() {
        super.onResume()

        viewModel.getProductApprovalCount()
    }

    private fun checkAttendance() {
        ApiServices.apiGET(
            Api.check_today_attendance,
            queue!!, token!!, object : ApiServiceListener {
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
                                    ViewUtils.startTracking(this@MainActivity, applicationContext)
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

                override fun onJSONResponseSuccess(response: JSONObject) {}

                override fun onNetworkResponseSuccess(response: NetworkResponse) {}

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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_order_summary) {
            if (userType.equals("TO", true) || userType.equals("ASM", true)) {
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
        } else if (id == R.id.menu_product_stock_request) {
            startActivity(Intent(this@MainActivity, StockRequestActivity::class.java))
        } else if (id == R.id.menu_request_approval) {
            startActivity(Intent(this@MainActivity, StockRequestApprovalActivity::class.java))
        } else if (id == R.id.menu_gift_summary) {
            startActivity(Intent(this@MainActivity, GiftSummaryActivity::class.java))
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun startLogoutObserve() {
        lifecycleScope.launch {
            viewModel.logoutResponse.observe(this@MainActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startLogoutObserve::Empty")

                        logoutDialog.show()
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startLogoutObserve::Error ${it.error}")
                        logoutDialog.dismiss()
                        toast(networkFailureMessage.handleFailure(it.error!!))

                        sharePrefUtils.clear()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startLogoutObserve::Loading")
                        if (!logoutDialog.isShowing) {
                            logoutDialog.show()
                        }
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startLogoutObserve:: Success ${it.data}")
                        logoutDialog.dismiss()

                        toast(it.data?.message ?: "")

                        sharePrefUtils.clear()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()

                    }
                }
            }
        }
    }

    private fun startApprovalCountObserve() {
        lifecycleScope.launch {
            viewModel.approvalCountResponse.observe(this@MainActivity) {
                when (it) {
                    is ApiState.Empty -> {
                        AppLogger.log("startApprovalCountObserve::Empty")
                    }

                    is ApiState.Error -> {
                        AppLogger.log("startApprovalCountObserve::Error ${it.error}")
//                        toast(networkFailureMessage.handleFailure(it.error!!))
                    }

                    is ApiState.Loading -> {
                        AppLogger.log("startApprovalCountObserve::Loading")
                    }

                    is ApiState.Success -> {
                        AppLogger.log("startApprovalCountObserve:: Success ${it.data}")

                        if (it.data?.data != null && it.data.data.pendingRequests > 99) {
                            tvApprovalCount.text = getString(R.string._99)
                        } else {
                            tvApprovalCount.text = it.data?.data?.pendingRequests.toString()
                        }

                        if (it.data?.data != null && it.data.data.notViewed > 99) {
                            binding.appContentMain.tvNotificationCount.isVisible = true
                            binding.appContentMain.tvNotificationCount.text =
                                getString(R.string._99)
                        } else {
                            if (it.data?.data?.notViewed == 0) {
                                binding.appContentMain.tvNotificationCount.isVisible = false
                            } else {
                                binding.appContentMain.tvNotificationCount.isVisible = true
                                binding.appContentMain.tvNotificationCount.text =
                                    it.data?.data?.notViewed.toString()

                            }
                        }
                    }
                }
            }
        }
    }

}