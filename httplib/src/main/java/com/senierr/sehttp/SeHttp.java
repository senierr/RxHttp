package com.senierr.sehttp;

import android.os.Handler;
import android.os.Looper;

import com.senierr.sehttp.interceptor.HttpLogInterceptor;
import com.senierr.sehttp.interceptor.LogLevel;
import com.senierr.sehttp.model.SSLParams;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.HttpUtil;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * SeHttp网络请求框架
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class SeHttp {
    // 默认超时时间
    public static final int DEFAULT_TIMEOUT = 30000;
    // 默认刷新时间
    public static final int REFRESH_MIN_INTERVAL = 100;

    private static volatile SeHttp seHttp;
    // 主线程调度器
    private Handler mainScheduler;
    // 网络请求对象
    private OkHttpClient.Builder okHttpClientBuilder;
    // 网络请求对象
    private OkHttpClient okHttpClient;
    // 公共请求参数
    private LinkedHashMap<String, String> commonUrlParams;
    // 公共请求头
    private LinkedHashMap<String, String> commonHeaders;
    // 超时重试次数
    private int retryCount;

    private SeHttp() {
        okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        okHttpClientBuilder.retryOnConnectionFailure(true);
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
     * get请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder get(String urlStr) {
        return new RequestBuilder("GET", urlStr);
    }

    /**
     * post请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder post(String urlStr) {
        return new RequestBuilder("POST", urlStr);
    }

    /**
     * head请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder head(String urlStr) {
        return new RequestBuilder("HEAD", urlStr);
    }

    /**
     * delete请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder delete(String urlStr) {
        return new RequestBuilder("DELETE", urlStr);
    }

    /**
     * put请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder put(String urlStr) {
        return new RequestBuilder("PUT", urlStr);
    }

    /**
     * patch请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder patch(String urlStr) {
        return new RequestBuilder("PATCH", urlStr);
    }

    /**
     * options请求
     *
     * @param urlStr
     * @return
     */
    public static RequestBuilder options(String urlStr) {
        return new RequestBuilder("OPTIONS", urlStr);
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
     * 获取公共参数
     *
     * @return
     */
    public LinkedHashMap<String, String> getCommonUrlParams() {
        return commonUrlParams;
    }

    /**
     * 获取公共头
     *
     * @return
     */
    public LinkedHashMap<String, String> getCommonHeaders() {
        return commonHeaders;
    }

    /**
     * 获取超时重连次数
     * 默认0次
     *
     * @return
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 设置连接超时
     *
     * @param connectTimeout
     * @return
     */
    public SeHttp connectTimeout(long connectTimeout) {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置读超时
     *
     * @param readTimeout
     * @return
     */
    public SeHttp readTimeout(long readTimeout) {
        okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 设置写超时
     *
     * @param writeTimeout
     * @return
     */
    public SeHttp writeTimeout(long writeTimeout) {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
        return this;
    }

    /**
     * 开启Debug
     *
     * @param tag
     * @param logLevel
     * @return
     */
    public SeHttp debug(String tag, LogLevel logLevel) {
        HttpLogInterceptor logInterceptor = new HttpLogInterceptor(tag, logLevel);
        okHttpClientBuilder.addInterceptor(logInterceptor);
        return this;
    }

    /**
     * 自定义域名访问规则
     *
     * @param hostnameVerifier
     * @return
     */
    public SeHttp hostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * 自定义SSL验证方式
     *
     * @param sslParams
     * @return
     */
    public SeHttp sslSocketFactory(SSLParams sslParams) {
        if (sslParams != null) {
            okHttpClientBuilder.sslSocketFactory(sslParams.getsSLSocketFactory(), sslParams.getTrustManager());
        }
        return this;
    }

    /**
     * 自定义cookie管理
     *
     * @param cookieJar
     * @return
     */
    public SeHttp cookieJar(CookieJar cookieJar) {
        okHttpClientBuilder.cookieJar(cookieJar);
        return this;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     * @return
     */
    public SeHttp addInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addInterceptor(interceptor);
        return this;
    }

    /**
     * 添加网络拦截器
     *
     * @param interceptor
     * @return
     */
    public SeHttp addNetworkInterceptor(Interceptor interceptor) {
        okHttpClientBuilder.addNetworkInterceptor(interceptor);
        return this;
    }

    /**
     * 添加单个公共请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public SeHttp addCommonUrlParam(String key, String value) {
        if (commonUrlParams == null) {
            commonUrlParams = new LinkedHashMap<>();
        }
        commonUrlParams.put(key, value);
        return this;
    }

    /**
     * 添加多个公共请求参数
     *
     * @param params
     * @return
     */
    public SeHttp addCommonUrlParams(LinkedHashMap<String, String> params) {
        commonUrlParams = HttpUtil.appendStringMap(commonUrlParams, params);
        return this;
    }

    /**
     * 添加单个公共头部
     *
     * @param key
     * @param value
     * @return
     */
    public SeHttp addCommonHeader(String key, String value) {
        if (commonHeaders == null) {
            commonHeaders = new LinkedHashMap<>();
        }
        commonHeaders.put(key, value);
        return this;
    }

    /**
     * 添加多个公共头部
     *
     * @param headers
     * @return
     */
    public SeHttp addCommonHeaders(LinkedHashMap<String, String> headers) {
        commonHeaders = HttpUtil.appendStringMap(commonHeaders, headers);
        return this;
    }

    /**
     * 设置超时重连次数
     *
     * @param retryCount
     * @return
     */
    public SeHttp retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }
}
