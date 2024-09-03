package com.barikoi.cnlapp.utils

import android.app.Application
import android.content.Context
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.utils.extension.NotificationOpenedHandler
import com.mapbox.mapboxsdk.Mapbox
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@HiltAndroidApp
class CNLApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this, null)
        appContext = applicationContext
        SentryAndroid.init(
            this
        ) { options: SentryAndroidOptions ->
            options.isEnableAutoSessionTracking = true
        }

        // TODO:: NEED TO CHANGE FOR OTHERS FLAVORS
        BarikoiTrace.initialize(this, Api.APIKEY)

        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

//         OneSignal Initialization
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        OneSignal.Notifications.addClickListener(NotificationOpenedHandler(this))

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }

    companion object {
        lateinit var appContext: Context
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(DefaultLocaleHelper.getInstance(newBase!!).onAttach())
    }
}