package com.iflytek.aikitdemo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import kotlin.properties.Delegates

class MyApp: Application() {

    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext
    }
}