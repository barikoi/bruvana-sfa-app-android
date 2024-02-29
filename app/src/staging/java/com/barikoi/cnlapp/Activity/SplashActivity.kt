package com.barikoi.cnlapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.barikoitrace.callback.BarikoiTraceUserCallback
import com.barikoi.barikoitrace.models.BarikoiTraceError
import com.barikoi.barikoitrace.models.BarikoiTraceUser
import com.barikoi.cnlapp.databinding.ActivitySplashBinding
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private var token: String? = ""
    private var mAppUpdateManager: AppUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mAppUpdateManager = AppUpdateManagerFactory.create(this)

        BarikoiTrace.initialize(applicationContext, Api.APIKEY_V2)

        token = sharePrefUtils.getString(Api.TOKEN)

        showProgress()

        mAppUpdateManager!!.appUpdateInfo.addOnSuccessListener { result ->
            AppLogger.log("AppUpdateService:1 " + result.updateAvailability())
            AppLogger.log("AppUpdateService:2 " + UpdateAvailability.UPDATE_AVAILABLE)
            AppLogger.log("AppUpdateService:3 " + result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
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
                    AppLogger.log("checkForAppUpdateAvailability")
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }

        mAppUpdateManager!!.appUpdateInfo.addOnFailureListener {
            AppLogger.log("checkForAppUpdate:: onFailure $it")
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
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                MULTIPLE_PERMISSIONS
            )
            return false
        } else {
//            traceInit()
            AppLogger.log("Splash:: request permission if list not empty")
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                init()
                handler.removeCallbacksAndMessages(null)
            }, 2000)

        }
        return true
    }

    private fun traceInit() {
        if (token != null && token!!.isNotEmpty()) {
            BarikoiTrace.setOrCreateUser(
                sharePrefUtils.getString(Api.NAME),
                sharePrefUtils.getString(Api.EMAIL),
                sharePrefUtils.getString(Api.PHONE),
                object : BarikoiTraceUserCallback {
                    override fun onFailure(barikoiError: BarikoiTraceError) {
                        AppLogger.log(
                            "traceInit:: User created onFailure: ${barikoiError.message}"
                        )
                    }

                    override fun onSuccess(traceUser: BarikoiTraceUser) {
                        AppLogger.log("traceInit:: User created: $traceUser")
                    }
                })
        }
    }

    fun init() {
        if (!token.equals("")) {
            routeToAppropriatePage(2)
        } else {
            hideProgress()
            if (sharePrefUtils.getBooleanWithDefaultTrue("isFirst")) {
                sharePrefUtils.saveBoolean("isFirst", false)
                routeToAppropriatePage(1)
            } else {
                routeToAppropriatePage(1)
            }
        }
    }

    private fun routeToAppropriatePage(routeOpt: Int) {
        when (routeOpt) {
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
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissionsList: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissionsList, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty()) {
                    var permissionsDenied = ""
                    for (per in permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += """
                            
                            $per
                            """.trimIndent()
                        }
                    }
                    // Show permissionsDenied
                    //sendLocation();
                    AppLogger.log("result: $requestCode")
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
    }

    override fun onResume() {
        super.onResume()
        mAppUpdateManager!!.appUpdateInfo.addOnSuccessListener { result ->
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
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }
        mAppUpdateManager!!.appUpdateInfo.addOnFailureListener {
            Sentry.captureMessage("checkForAppUpdate onFailure onResume")
            AppLogger.log("checkForAppUpdate onFailure $it")
            checkPermissions()
        }

    }

    companion object {
        const val MULTIPLE_PERMISSIONS = 10
        const val RC_APP_UPDATE = 11
    }
}