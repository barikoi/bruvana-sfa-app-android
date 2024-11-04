package com.barikoi.cnlapp.utils

import android.app.Application
import android.content.Context
import com.barikoi.barikoitrace.BarikoiTrace
import com.barikoi.cnlapp.BuildConfig
import com.barikoi.cnlapp.utils.extension.NotificationOpenedHandler
import com.mapbox.mapboxsdk.Mapbox
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.IDisplayableNotification
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent
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
        Mapbox.getInstance(this)
        SentryAndroid.init(this) { options: SentryAndroidOptions ->
            options.dsn = BuildConfig.sentryDNS
            options.isEnableAutoSessionTracking = true
        }

        // TODO:: NEED TO CHANGE FOR OTHERS FLAVORS
        BarikoiTrace.initialize(this, Api.APIKEY)


//         OneSignal Initialization
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        OneSignal.Notifications.addForegroundLifecycleListener(object :
            INotificationLifecycleListener {
            override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                val notification: IDisplayableNotification = event.notification
                event.preventDefault()
                notification.display()
            }

        })
        OneSignal.Notifications.addClickListener(NotificationOpenedHandler(this))

        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(false)
        }
    }
}