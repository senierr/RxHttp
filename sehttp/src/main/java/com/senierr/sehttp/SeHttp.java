package com.senierr.sehttp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.senierr.sehttp.interceptor.HttpLogInterceptor;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.HttpUtil;
import com.senierr.sehttp.util.SeLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class SeHttp {
    // 默认超时时间
    private static final int DEFAULT_MILLISECONDS = 30000;

    private static volatile SeHttp seHttp;
    // 主线程调度器
    private Handler mainScheduler;
    // 网络请求对象
    private OkHttpClient.Builder okHttpClientBuilder;
    // 网络请求对象
    private OkHttpClient okHttpClient;
    // 公共请求参数
    private Map<String, String> commonParams;
    // 公共请求头
    private Map<String, String> commonHeaders;
    // 超时重试次数
    private int retryCount;

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
     * 获取公共参数
     *
     * @return
     */
    public Map<String, String> getCommonParams() {
        return commonParams;
    }

    /**
     * 获取公共头
     *
     * @return
     */
    public Map<String, String> getCommonHeaders() {
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
    public SeHttp debug(String tag) {
        HttpLogInterceptor logInterceptor = new HttpLogInterceptor();
        logInterceptor.setPrintLevel(HttpLogInterceptor.LEVEL_BODY);
        logInterceptor.setColorLevel(Log.INFO);
        logInterceptor.setPrintTag(tag);
        okHttpClientBuilder.addInterceptor(logInterceptor);
        SeLogger.init(tag);
        return this;
    }

    /**
     * 设置Debug模式，可设置是否打印错误日志
     *
     * @param tag
     * @param isLogException
     * @return
     */
    public SeHttp debug(String tag, boolean isLogException) {
        HttpLogInterceptor logInterceptor = new HttpLogInterceptor();
        logInterceptor.setPrintLevel(HttpLogInterceptor.LEVEL_BODY);
        logInterceptor.setColorLevel(Log.INFO);
        logInterceptor.setPrintTag(tag);
        okHttpClientBuilder.addInterceptor(logInterceptor);
        SeLogger.init(isLogException, tag);
        return this;
    }

    /**
     * 自定义域名访问规则
     *
     * @param hostnameVerifier
     * @return
     */
    public SeHttp setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
        return this;
    }

    /**
     * 添加单个公共请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public SeHttp addCommonParam(String key, String value) {
        if (commonParams == null) {
            commonParams = new HashMap<>();
        }
        commonParams.put(key, value);
        return this;
    }

    /**
     * 添加多个公共请求参数
     *
     * @param params
     * @return
     */
    public SeHttp addCommonParams(Map<String, String> params) {
        commonParams = HttpUtil.appendMap(commonParams, params);
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
            commonHeaders = new HashMap<>();
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
    public SeHttp addCommonHeaders(Map<String, String> headers) {
        commonHeaders = HttpUtil.appendMap(commonHeaders, headers);
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
     * 设置超时重连次数
     *
     * @param retryCount
     * @return
     */
    public SeHttp setRetryCount(int retryCount) {
        this.retryCount = retryCount;
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

}
