package com.senierr.simple

import android.app.Application
import com.senierr.sehttp.SeHttp
import com.senierr.sehttp.cookie.SPCookieJar
import com.senierr.sehttp.https.SSLFactory
import com.senierr.sehttp.https.UnSafeHostnameVerifier
import com.senierr.sehttp.util.HttpLogInterceptor

/**
 * 应用入口
 *
 * @author zhouchunjie
 * @date 2018/7/27
 */
class SeApplication : Application() {

    companion object {
        @JvmStatic lateinit var instance: SeApplication
            private set
    }

    private lateinit var seHttp: SeHttp

    override fun onCreate() {
        super.onCreate()
        instance = this

        initHttp()
    }

    /**
     * 初始化网络请求器
     */
    private fun initHttp() {
        seHttp = SeHttp.Builder()
                .debug("SeHttp", HttpLogInterceptor.LogLevel.BODY)
                .connectTimeout(10 * 1000)
                .readTimeout(10 * 1000)
                .writeTimeout(10 * 1000)
                .addCommonHeader("com_header", "com_header_value")
                .addCommonHeader("language", "English")
                .addCommonUrlParam("com_url_param", "com_url_param_value")
                .sslFactory(SSLFactory())
                .hostnameVerifier(UnSafeHostnameVerifier())
                .cookieJar(SPCookieJar(this))
                .refreshInterval(200)
                .retryCount(3)
                .build()
    }

    fun getHttp(): SeHttp {
        return seHttp
    }
}