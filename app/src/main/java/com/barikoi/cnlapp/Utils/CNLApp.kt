package com.barikoi.cnlapp.Utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

class CNLApp: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
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