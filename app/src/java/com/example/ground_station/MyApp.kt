package com.iflytek.aikitdemo

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.blankj.utilcode.BuildConfig
import com.blankj.utilcode.util.AppUtils
import java.com.example.ground_station.data.crash.CrashHandler
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


        // 保存崩溃日志
        // 保存崩溃日志
        CrashHandler.getInstance().init(
            applicationContext,
            AppUtils.getAppName(),
            AppUtils.getAppVersionName(),
            "", "", "版本类型(release)：" + BuildConfig.DEBUG + " "
        )
    }
}