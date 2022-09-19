package com.barikoi.cnlapp.Activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.Utils.Api
import com.github.ybq.android.spinkit.style.ThreeBounce
import java.util.*

class SplashActivity : AppCompatActivity() {

    private var token : String?= ""
    private var userId: String? = ""
    private var isFirst = true
    private var MULTIPLE_PERMISSIONS = 10
    private var progressBar: ProgressBar? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()



        token = prefs!!.getString(Api.TOKEN, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        isFirst = prefs!!.getBoolean("isFirst", true)
        val user_name = prefs!!.getString(Api.NAME, "")
        val user_email = prefs!!.getString(Api.EMAIL, "")
        val user_phone = prefs!!.getString(Api.PHONE, "")

        progressBar = findViewById(R.id.progressBar)
        progressBar!!.setIndeterminateDrawable(ThreeBounce())
        showProgress()

        checkPermissions()
        //Handler().postDelayed(Runnable { init() }, 2000)
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ActivityCompat.checkSelfPermission(this, p)
            //            Log.d("Verifyf", "user LatLoc result: " +result);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            //sendLocation();
            //init()
            return false
        }
       /* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                *//*ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                return false;*//*
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setCancelable(false)
                alertBuilder.setIcon(R.drawable.mapmarkersplash)
                alertBuilder.setTitle("Background permission is necessary")
                alertBuilder.setMessage(resources.getString(R.string.app_name) + " needs background location permission to get location data. Kindly select ALLOW ALL THE TIME option to stay connected.")
                alertBuilder.setPositiveButton(
                    android.R.string.yes
                ) { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@SplashActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        1
                    )
                    //proceedusercheck();
                }
                val alert = alertBuilder.create()
                alert.show()
                Log.d("Splash", "background permission not granted: " + ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            } else {
                Log.d("Splash", "background permission granted")
                Handler().postDelayed(Runnable { init() }, 2000)
            }
        } */
        else {
            Log.d("Splash", "request permission if list not empty")
            Handler().postDelayed(Runnable { init() }, 1000)
        }
        return true
    }

    fun init(){
        if (!token.equals("")){
            routeToAppropriatePage(2)
        }else{
            if (isFirst) {
                hideProgress()
                val prefs = PreferenceManager.getDefaultSharedPreferences(
                    applicationContext
                )
                val editor = prefs.edit()
                editor.putBoolean("isFirst", false)
                editor.commit()
                routeToAppropriatePage(1)
            } else {
                hideProgress()
                //Toast.makeText(this, getString(R.string.no_auth_token), Toast.LENGTH_SHORT).show()
                routeToAppropriatePage(1)
            }
        }
    }

    private fun routeToAppropriatePage(routeopt: Int) {
        // Example routing
        when (routeopt) {
            0 -> {
                /*val i = Intent(this, SignUpActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()*/
            }
            1 -> {
                val i = Intent(this, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
                finish()
            }
            2 -> {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
    }
    private fun showProgress() {
        progressBar!!.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progressBar!!.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.size > 0) {
                    var permissionsDenied = ""
                    val count = 0
                    for (per in permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += """
                            
                            $per
                            """.trimIndent()
                            //init()
                        }
                        /*else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    val alertBuilder = AlertDialog.Builder(this)
                                    alertBuilder.setCancelable(false)
                                    alertBuilder.setIcon(R.drawable.descologo)
                                    alertBuilder.setTitle("Background permission is necessary")
                                    alertBuilder.setMessage(resources.getString(R.string.app_name) + " needs background location permission to get location data. Kindly select ALLOW ALL THE TIME option to stay connected.")
                                    alertBuilder.setPositiveButton(
                                        android.R.string.yes
                                    ) { dialog, which ->
                                        ActivityCompat.requestPermissions(
                                            this@SplashActivity,
                                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
                                        //proceedusercheck();
                                    }
                                    val alert = alertBuilder.create()

                                    //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                                    Log.d("Dialog", "check Location permission: 3 count: " + count + " " +permissionsList.size)
                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                                        if (count == permissionsList.size) {
                                            Log.d("Dialog", "check Location permission: 3")
                                            alert.show()
                                        }
                                    } else {
                                        Log.d("Dialog", "check Location permission: 4")
                                        alert.dismiss()
                                    }
                                }
                            } else {
                                init()
                            }
                        }*/
                        //init()
                    }
                    // Show permissionsDenied
                    //sendLocation();
                    Log.d("MainActivity", "result: $requestCode")
                    init()
                }
                return
            }
        }
    }

    private fun setLocale(lang: String) {
        val myLocale = Locale(lang)
        Locale.setDefault(myLocale)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        //restartActivity()
    }

    override fun onResume() {
        super.onResume()
        //checkPermissions()
        //init()

    }
}