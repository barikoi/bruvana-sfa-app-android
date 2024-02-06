package com.barikoi.cnlapp.Activity

import android.Manifest
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.R
import com.barikoi.cnlapp.utils.Api
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import io.sentry.Sentry
import java.util.*

class SplashActivity : AppCompatActivity() {

    private var token : String?= ""
    private var userId: String? = ""
    private var isFirst = true
    private var MULTIPLE_PERMISSIONS = 10
    private var progressBar: ProgressBar? = null
    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val RC_APP_UPDATE = 11
    var mAppUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mAppUpdateManager = AppUpdateManagerFactory.create(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        editor = prefs!!.edit()

        BarikoiTrace.initialize(this, Api.APIKEY)

        token = prefs!!.getString(Api.TOKEN, "")
        userId = prefs!!.getString(Api.USER_ID, "")
        isFirst = prefs!!.getBoolean("isFirst", true)
        val user_name = prefs!!.getString(Api.NAME, "")
        val user_email = prefs!!.getString(Api.EMAIL, "")
        val user_phone = prefs!!.getString(Api.PHONE, "")

        progressBar = findViewById(R.id.progressBar)
        progressBar!!.setIndeterminateDrawable(ThreeBounce())
        showProgress()

        mAppUpdateManager!!.appUpdateInfo.addOnSuccessListener { result ->
            println("AppUpdateService:1 " + result.updateAvailability())
            println("AppUpdateService:2 " + UpdateAvailability.UPDATE_AVAILABLE)
            println("AppUpdateService:3 " + result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    mAppUpdateManager!!.startUpdateFlowForResult(
                        result,
                        AppUpdateType.IMMEDIATE,
                        this@SplashActivity,
                        RC_APP_UPDATE
                    )
                    println("checkForAppUpdateAvailability")
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }

        mAppUpdateManager!!.appUpdateInfo.addOnFailureListener {
            println("checkForAppUpdate onFailure")
            checkPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
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
            return false
        }
        else {
            Log.d("Splash", "request permission if list not empty")
            val handler: Handler = Handler()
            handler.postDelayed(Runnable {
                init()
                handler.removeCallbacksAndMessages(null) }, 2000)
            //init()

        }
        return true
    }

    fun init(){
        if (!token.equals("")){
            routeToAppropriatePage(2)
        }else{
            if (isFirst) {
                hideProgress()
                val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
        mAppUpdateManager!!.appUpdateInfo.addOnSuccessListener { result ->
            println("AppUpdateService:1 " + result.updateAvailability())
            println("AppUpdateService:2 " + UpdateAvailability.UPDATE_AVAILABLE)
            println("AppUpdateService:3 " + result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    mAppUpdateManager!!.startUpdateFlowForResult(
                        result,
                        AppUpdateType.IMMEDIATE,
                        this@SplashActivity,
                        RC_APP_UPDATE
                    )
                    println("checkForAppUpdateAvailability")
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }
        mAppUpdateManager!!.appUpdateInfo.addOnFailureListener {
            Sentry.captureMessage("checkForAppUpdate onFailure onResume")
            println("checkForAppUpdate onFailure")
            checkPermissions()
        }

    }
}