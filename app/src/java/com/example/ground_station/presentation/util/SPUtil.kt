package com.iflytek.aikitdemo.tool

import android.app.Activity
import android.content.SharedPreferences
import com.iflytek.aikitdemo.MyApp

object SPUtil {

    fun putBase(keyName: String, value: Any): Boolean? {
        val sharedPreferences = getSp()
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        when (value) {
            is Int -> editor?.putInt(keyName, value)
            is Boolean -> editor?.putBoolean(keyName, value)
            is Float -> editor?.putFloat(keyName, value)
            is String -> editor?.putString(keyName, value)
            is Long -> editor?.putLong(keyName, value)
            else -> throw IllegalArgumentException("SharedPreferences can,t be save this type")
        }
        return editor?.commit()
    }

    fun getBoolean(keyName: String, defaultValue: Boolean = false): Boolean {
        val sharedPreferences = getSp()
        return sharedPreferences?.getBoolean(keyName, defaultValue) ?: false
    }

    fun getInt(keyName: String, defaultValue: Int = 0): Int {
        val sharedPreferences = getSp()
        return sharedPreferences?.getInt(keyName, defaultValue) ?: 0
    }

    fun getString(keyName: String, defaultValue: String? = null): String {
        val sharedPreferences = getSp()
        return sharedPreferences?.getString(keyName, defaultValue) ?: ""
    }

    private fun getSp(): SharedPreferences? {
        return MyApp.CONTEXT.getSharedPreferences(MyApp.CONTEXT.packageName, Activity.MODE_PRIVATE)
    }
}