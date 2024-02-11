package com.barikoi.cnlapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class SharePrefUtils(
    context: Context
) {
    private var sharedPreferences: SharedPreferences? = null

    init {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
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

    fun saveInt(key: String, value: Int) {
        sharedPreferences?.edit()?.putInt(key, value)?.apply()
    }

    fun getInt(key: String): Int? {
        return sharedPreferences?.getInt(key, 0)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences?.getBoolean(key, false)!!
    }

    fun getBooleanWithDefaultTrue(key: String): Boolean {
        return sharedPreferences?.getBoolean(key, true)!!
    }

    fun clear() {
        sharedPreferences?.edit()?.clear()?.apply()
    }
}