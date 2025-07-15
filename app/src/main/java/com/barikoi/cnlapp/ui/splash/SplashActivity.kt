package com.barikoi.cnlapp.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.databinding.ActivitySplashBinding
import com.barikoi.cnlapp.ui.auth.LoginActivity
import com.barikoi.cnlapp.ui.main.MainActivity
import com.barikoi.cnlapp.utils.Api
import com.barikoi.cnlapp.utils.AppLogger
import com.barikoi.cnlapp.utils.SharePrefUtils
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import io.sentry.Sentry
import javax.inject.Inject

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var sharePrefUtils: SharePrefUtils

    private var token: String? = ""
    private lateinit var mAppUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAppUpdateManager = AppUpdateManagerFactory.create(this)

        BarikoiTrace.initialize(applicationContext, BuildConfig.TRACE_API_KEY)

        token = sharePrefUtils.getString(Api.TOKEN)

        binding.progressBar.isVisible = true
        try {
            throw Exception("This is a test.")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }


        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { result ->
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        result,
                        AppUpdateType.IMMEDIATE,
                        this@SplashActivity,
                        RC_APP_UPDATE
                    )
                    AppLogger.log("checkForAppUpdateAvailability")
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }

        mAppUpdateManager.appUpdateInfo.addOnFailureListener {
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
            AppLogger.log("Splash:: request permission if list not empty")
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                init()
                handler.removeCallbacksAndMessages(null)
            }, 2000)

        }
        return true
    }

    fun init() {
        if (!token.equals("")) {
            routeToAppropriatePage(2)
        } else {
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
            1 -> {
                val i = Intent(this, LoginActivity::class.java)
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
                    AppLogger.log("result: $requestCode")
                    init()
                }
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mAppUpdateManager.appUpdateInfo.addOnSuccessListener { result ->
            if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                        result,
                        AppUpdateType.IMMEDIATE,
                        this@SplashActivity,
                        RC_APP_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            } else {
                checkPermissions()
            }
        }
        mAppUpdateManager.appUpdateInfo.addOnFailureListener {
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