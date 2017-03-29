package com.senierr.sehttp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.senierr.sehttp.interceptor.HttpLogInterceptor;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.SeLogger;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class SeHttp {

    // 默认超时时间
    private static final int DEFAULT_MILLISECONDS = 30000;
    // 异步刷新时间间隔
    public static final long REFRESH_INTERVAL = 100;

    // 主线程调度器
    private Handler mainScheduler;
    // 网络请求对象
    private OkHttpClient.Builder okHttpClientBuilder;
    // 网络请求对象
    private OkHttpClient okHttpClient;

    private static volatile SeHttp seHttp;

    private SeHttp() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.connectTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        mainScheduler = new Handler(Looper.getMainLooper());
    }

    public static SeHttp getInstance() {
        if (seHttp == null) {
            synchronized (SeHttp.class) {
                if (seHttp == null) {
                    seHttp = new SeHttp();
                }
            }
        }
        return seHttp;
    }

    /**
     * 获取主线程调度器
     *
     * @return
     */
    public Handler getMainScheduler() {
        return mainScheduler;
    }

    /**
     * 获取OkHttp请求对象
     *
     * @return
     */
    public OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = okHttpClientBuilder.build();
        }
        return okHttpClient;
    }

    /**
     * 设置连接超时
     *
     * @param connectTimeout
     * @return
     */
    public SeHttp setConnectTimeout(long connectTimeout) {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置读超时
     *
     * @param readTimeout
     * @return
     */
    public SeHttp setReadTimeout(long readTimeout) {
        okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置写超时
     *
     * @param writeTimeout
     * @return
     */
    public SeHttp setWriteTimeout(long writeTimeout) {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置Debug模式
     *
     * @param tag
     * @return
     */
    public SeHttp setDebug(String tag) {
        HttpLogInterceptor logInterceptor = new HttpLogInterceptor();
        logInterceptor.setPrintLevel(HttpLogInterceptor.LEVEL_BODY);
        logInterceptor.setColorLevel(Log.INFO);
        logInterceptor.setPrintTag(tag);
        okHttpClientBuilder.addInterceptor(logInterceptor);
        SeLogger.init(true, tag);
        return this;
    }

    /**
     * 根据tag取消请求
     *
     * @param tag
     */
    public void cancelTag(Object tag) {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 取消所有请求
     */
    public void cancelAll() {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    public static RequestBuilder get(String urlStr) {
        return new RequestBuilder("GET", urlStr);
    }

    public static RequestBuilder post(String urlStr) {
        return new RequestBuilder("POST", urlStr);
    }
}
