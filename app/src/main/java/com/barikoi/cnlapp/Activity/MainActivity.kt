package com.barikoi.cnlapp.Activity

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.barikoi.cnlapp.Fragment.HomeFragment
import com.barikoi.cnlapp.Fragment.MapFragment
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.barikoi.cnlapp.Utils.RequestQueueSingleton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawer: DrawerLayout? = null
    private var navigationDrawer: NavigationView? = null
    private var menu_drawer: ImageView? = null
    private var tvUserName: TextView? = null
    private var tvTitle: TextView? = null
    private var token : String?= ""
    private var userId: String? = ""
    private var userName: String? = ""
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var nav_view: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()

        token = prefs!!.getString(Api.TOKEN, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        userName = prefs!!.getString(Api.NAME, "")
        tvTitle = findViewById(R.id.tvTitle)
        nav_view = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        //nav_view!!.setSelectedItemId(R.id.navigation_map)

        navigationDrawer = findViewById(R.id.nav_view)
        navigationDrawer!!.setNavigationItemSelectedListener(this)
        drawer = findViewById(R.id.drawer_layout)
        /*drawer.useCustomBehavior(Gravity.START)
        drawer.setViewScale(Gravity.START, 0.9f)
        drawer.setViewElevation(Gravity.START, 20f)
        drawer.setRadius(Gravity.START, 25f)*/
        //drawer!!.openDrawer(GravityCompat.START, true)
        menu_drawer = findViewById<ImageView>(R.id.drawer)
        menu_drawer!!.setOnClickListener(View.OnClickListener {
            drawer!!.openDrawer(
                GravityCompat.START,
                true
            )
        })

        tvTitle!!.text = "Map"
        setCurrentFragment(MapFragment(), this@MainActivity)

        val header = navigationDrawer!!.getHeaderView(0)
        tvUserName = header.findViewById<TextView>(R.id.textView_username)
        tvUserName!!.text = userName

        //setCurrentFragment(HomeFragment(), this@MainActivity)

        nav_view!!.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    tvTitle!!.text = "Home"
                    setCurrentFragment(HomeFragment(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_map -> {
                    tvTitle!!.text = "Map"
                    setCurrentFragment(MapFragment(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }
                /*R.id.navigation_announcement -> {
                    MainActivity.setCurrentFragment(FragmentAnnouncement(), this@MainActivity)
                    return@OnNavigationItemSelectedListener true
                }*/
            }
            false
        })

    }
    fun setCurrentFragment(fragment: Fragment?, activity: Activity) {
        val fragmentManager = (activity as FragmentActivity).supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentLayout, fragment!!)
        fragmentTransaction.commit()
        //fragmentManager.executePendingTransactions()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_shop_create) {
            //startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        } else if (id == R.id.menu_route_list) {
            startActivity(Intent(this@MainActivity, RouteActivity::class.java))
        } else if (id == R.id.menu_offer_list){

        }else if (id == R.id.menu_order_history){

        }else if (id == R.id.menu_attendance){

        } else if (id == R.id.menu_logout) {
            AlertDialog.Builder(this@MainActivity, R.style.AlertDialog)
                .setTitle(R.string.logout)
                .setMessage(R.string.sure_log_out)
                .setPositiveButton(android.R.string.yes,
                    DialogInterface.OnClickListener { dialog, which ->
                        logout(this@MainActivity)
                    })
                .setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> }) // do nothing
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
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
                            //Sentry.captureException(e)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //Sentry.captureException(e)
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

}