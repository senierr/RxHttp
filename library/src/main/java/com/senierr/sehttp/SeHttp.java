package com.senierr.sehttp;

import android.os.Handler;
import android.os.Looper;

import com.senierr.sehttp.cache.CacheInterceptor;
import com.senierr.sehttp.cookie.ClearableCookieJar;
import com.senierr.sehttp.https.SSLFactory;
import com.senierr.sehttp.internal.RequestFactory;
import com.senierr.sehttp.util.HttpLogInterceptor;
import com.senierr.sehttp.util.Utils;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * SeHttp
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class SeHttp {

    // 默认超时时间
    public static final int DEFAULT_TIMEOUT = 30 * 1000;
    // 默认刷新时间
    public static final int REFRESH_MIN_INTERVAL = 100;

    // 公共请求参数
    private LinkedHashMap<String, String> commonUrlParams;
    // 公共请求头
    private LinkedHashMap<String, String> commonHeaders;
    // 超时重试次数
    private int retryCount;
    // 主线程调度器
    private Handler mainScheduler;
    // 异步刷新间隔
    private int refreshInterval;
    // 网络请求器
    private OkHttpClient okHttpClient;

    private SeHttp(Builder builder) {
        this.commonUrlParams = builder.commonUrlParams;
        this.commonHeaders = builder.commonHeaders;
        this.retryCount = builder.retryCount;
        this.mainScheduler = builder.mainScheduler;
        this.refreshInterval = builder.refreshInterval;
        okHttpClient = builder.okHttpClientBuilder.build();
    }

    /** get请求 */
    public RequestFactory get(String urlStr) {
        return method("GET", urlStr);
    }

    /** post请求 */
    public RequestFactory post(String urlStr) {
        return method("POST", urlStr);
    }

    /** head请求 */
    public RequestFactory head(String urlStr) {
        return method("HEAD", urlStr);
    }

    /** delete请求 */
    public RequestFactory delete(String urlStr) {
        return method("DELETE", urlStr);
    }

    /** put请求 */
    public RequestFactory put(String urlStr) {
        return method("PUT", urlStr);
    }

    /** options请求 */
    public RequestFactory options(String urlStr) {
        return method("OPTIONS", urlStr);
    }

    /** 自定义请求 */
    public RequestFactory method(String method, String urlStr) {
        return new RequestFactory(this, method, urlStr);
    }

    /** 取消请求 */
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

    /** 取消所有请求 */
    public void cancelAll() {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    public LinkedHashMap<String, String> getCommonUrlParams() {
        return commonUrlParams;
    }

    public LinkedHashMap<String, String> getCommonHeaders() {
        return commonHeaders;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Handler getMainScheduler() {
        return mainScheduler;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public ClearableCookieJar getCookieJar() {
        return (ClearableCookieJar) getOkHttpClient().cookieJar();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public final static class Builder {
        private LinkedHashMap<String, String> commonUrlParams;
        private LinkedHashMap<String, String> commonHeaders;
        private int retryCount;
        private Handler mainScheduler;
        private int refreshInterval;
        private OkHttpClient.Builder okHttpClientBuilder;

        public Builder() {
            okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.retryOnConnectionFailure(true);
            mainScheduler = new Handler(Looper.getMainLooper());
            refreshInterval = REFRESH_MIN_INTERVAL;
        }

        public SeHttp build() {
            return new SeHttp(this);
        }

        /** 自定义配置 **/
        public Builder addCommonUrlParam(String key, String value) {
            if (commonUrlParams == null) {
                commonUrlParams = new LinkedHashMap<>();
            }
            commonUrlParams.put(key, value);
            return this;
        }

        public Builder addCommonUrlParams(LinkedHashMap<String, String> params) {
            commonUrlParams = Utils.mergeMap(commonUrlParams, params);
            return this;
        }

        public Builder addCommonHeader(String key, String value) {
            if (commonHeaders == null) {
                commonHeaders = new LinkedHashMap<>();
            }
            commonHeaders.put(key, value);
            return this;
        }

        public Builder addCommonHeaders(LinkedHashMap<String, String> headers) {
            commonHeaders = Utils.mergeMap(commonUrlParams, headers);
            return this;
        }

        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder refreshInterval(int refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }

        /** okHttpClientBuilder配置 **/
        public Builder debug(String tag, HttpLogInterceptor.LogLevel logLevel) {
            HttpLogInterceptor logInterceptor = new HttpLogInterceptor(tag, logLevel);
            okHttpClientBuilder.addInterceptor(logInterceptor);
            okHttpClientBuilder.addInterceptor(new CacheInterceptor());
            return this;
        }

        public Builder connectTimeout(long connectTimeout) {
            okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder readTimeout(long readTimeout) {
            okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder writeTimeout(long writeTimeout) {
            okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        public Builder sslFactory(SSLFactory sslFactory) {
            if (sslFactory != null) {
                okHttpClientBuilder.sslSocketFactory(sslFactory.getsSLSocketFactory(), sslFactory.getTrustManager());
            }
            return this;
        }

        public Builder cookieJar(ClearableCookieJar cookieJar) {
            okHttpClientBuilder.cookieJar(cookieJar);
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addNetworkInterceptor(interceptor);
            return this;
        }
    }
}
