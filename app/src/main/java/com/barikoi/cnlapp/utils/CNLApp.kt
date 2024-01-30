package com.barikoi.cnlapp.utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import io.sentry.android.core.SentryAndroidOptions
import java.util.Locale

@HiltAndroidApp
class CNLApp: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        SentryAndroid.init(
            this
        ) { options: SentryAndroidOptions ->
            options.isEnableAutoSessionTracking = true
        }

        /*// Replace YOUR_ENVIRONMENT_ID with the ID of the Heap environment you wish to send data to.
        Heap.startRecording(this, "2261727323")
        // Call ViewAutocaptureSDK.register() to enable autocapture for supported UI elements.
        ViewAutocaptureSDK.register()*/
    }

    companion object {

        lateinit  var appContext: Context

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val res: Resources = base!!.resources

        val locale = Locale("en")
        Locale.setDefault(locale)

        val config = Configuration()
        config.locale = locale

        res.updateConfiguration(config, res.getDisplayMetrics())
    }
}