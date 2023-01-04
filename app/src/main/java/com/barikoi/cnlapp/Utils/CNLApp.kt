package com.barikoi.cnlapp.Utils

import android.app.Application
import android.content.Context

class CNLApp: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {

        lateinit  var appContext: Context

    }
}