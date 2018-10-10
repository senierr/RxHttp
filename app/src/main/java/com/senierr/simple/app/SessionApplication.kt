package com.senierr.simple.app

import android.app.Application
import com.senierr.http.RxHttp
import com.senierr.http.internal.LogInterceptor

class SessionApplication : Application() {

    companion object {
        private const val APP_ID_KEY = "X-Bmob-Application-Id"
        private const val APP_ID_VALUE = "bb17cb62e3293d3ce5462c0421c06eb7"
        private const val REST_API_KEY = "X-Bmob-REST-API-Key"
        private const val REST_API_VALUE = "5007a55d8fb84f92835172f8abc20cc4"

        private const val DEBUG_TAG = "Repository"
        private const val TIMEOUT = 15 * 1000L

        lateinit var application: SessionApplication
            private set
    }

    lateinit var dataHttp: RxHttp

    override fun onCreate() {
        super.onCreate()
        application = this

        dataHttp = RxHttp.Builder()
                .debug(DEBUG_TAG, LogInterceptor.LogLevel.BODY)
                .addCommonHeader(APP_ID_KEY, APP_ID_VALUE)
                .addCommonHeader(REST_API_KEY, REST_API_VALUE)
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .build()
    }
}
