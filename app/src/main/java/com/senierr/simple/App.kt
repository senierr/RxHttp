package com.senierr.simple

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 *
 * @author zhouchunjie
 * @date 2019/4/25 21:19
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to Perflib for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }
}