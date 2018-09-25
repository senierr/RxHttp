package com.senierr.simple.repository

import android.content.Context
import android.content.SharedPreferences
import com.senierr.http.RxHttp
import com.senierr.http.internal.LogInterceptor
import com.senierr.simple.repository.remote.APP_ID_KEY
import com.senierr.simple.repository.remote.APP_ID_VALUE
import com.senierr.simple.repository.remote.REST_API_KEY
import com.senierr.simple.repository.remote.REST_API_VALUE
import com.senierr.simple.repository.service.api.INoteService
import com.senierr.simple.repository.service.impl.NoteService

/**
 * 数据服务入口
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
object Repository {

    private const val DEBUG_TAG = "Repository"
    private const val TIMEOUT = 15 * 1000L

    private const val SP_NAME = "HideActive"

    internal lateinit var dataHttp: RxHttp
    internal lateinit var sp: SharedPreferences

    /**
     * 数据层初始化
     */
    fun initialize(context: Context) {
        // 网络请求
        dataHttp = RxHttp.Builder()
                .debug(DEBUG_TAG, LogInterceptor.LogLevel.BODY)
                .addCommonHeader(APP_ID_KEY, APP_ID_VALUE)
                .addCommonHeader(REST_API_KEY, REST_API_VALUE)
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .build()
        // SharedPreferences
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 获取数据服务
     */
    inline fun <reified T> getService(): T = when(T::class.java) {
        INoteService::class.java ->
            NoteService() as T
        else -> throw IllegalArgumentException("Can not find this type of the service!")
    }
}