package com.barikoi.cnlapp.utils

import android.content.Context
import android.content.SharedPreferences


class SharePrefUtils(
    context: Context
) {
    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences =
            context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences?.getString(key, "")
    }

    fun saveLong(key: String, value: Long) {
        sharedPreferences?.edit()?.putLong(key, value)?.apply()
    }

    fun getLong(key: String): Long? {
        return sharedPreferences?.getLong(key, 0)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String): Boolean? {
        return sharedPreferences?.getBoolean(key, false)
    }

    fun clear() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
}