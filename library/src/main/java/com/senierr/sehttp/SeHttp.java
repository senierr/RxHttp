package com.senierr.sehttp;

import android.os.Handler;
import android.os.Looper;

import com.senierr.sehttp.internal.RequestBuilder;
import com.senierr.sehttp.util.HttpLogInterceptor;
import com.senierr.sehttp.util.HttpUtil;
import com.senierr.sehttp.https.SSLFactory;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * SeHttp
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class SeHttp {

    // 构造器
    private Builder builder;

    private SeHttp(Builder builder) {
        this.builder = builder;
    }

    public RequestBuilder get(String urlStr) {
        return method("GET", urlStr);
    }

    public RequestBuilder post(String urlStr) {
        return method("POST", urlStr);
    }

    public RequestBuilder head(String urlStr) {
        return method("HEAD", urlStr);
    }

    public RequestBuilder delete(String urlStr) {
        return method("DELETE", urlStr);
    }

    public RequestBuilder put(String urlStr) {
        return method("PUT", urlStr);
    }

    public RequestBuilder options(String urlStr) {
        return method("OPTIONS", urlStr);
    }

    public RequestBuilder method(String method, String urlStr) {
        return new RequestBuilder(this, method, urlStr);
    }

    public void cancelTag(Object tag) {
        for (Call call : builder.getOkHttpClient().dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : builder.getOkHttpClient().dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public void cancelAll() {
        for (Call call : builder.getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : builder.getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    public Builder getBuilder() {
        return builder;
    }

    public final static class Builder {
        // 默认超时时间
        private static final int DEFAULT_TIMEOUT = 30 * 1000;
        // 默认刷新时间
        private static final int REFRESH_MIN_INTERVAL = 100;
        // 公共请求参数
        private LinkedHashMap<String, String> commonUrlParams;
        // 公共请求头
        private LinkedHashMap<String, String> commonHeaders;
        // 超时重试次数
        private int retryCount;
        // 主线程调度器
        private Handler mainScheduler;
        // 异步刷新间隔
        private int refreshInterval = REFRESH_MIN_INTERVAL;
        // 网络请求构造器
        private OkHttpClient.Builder okHttpClientBuilder;
        // 网络请求器
        private OkHttpClient okHttpClient;

        public Builder() {
            okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.retryOnConnectionFailure(true);
            mainScheduler = new Handler(Looper.getMainLooper());
        }

        public SeHttp build() {
            return new SeHttp(this);
        }

        public Builder setConnectTimeout(long connectTimeout) {
            okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder setReadTimeout(long readTimeout) {
            okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder setWriteTimeout(long writeTimeout) {
            okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder addCommonUrlParam(String key, String value) {
            if (commonUrlParams == null) {
                commonUrlParams = new LinkedHashMap<>();
            }
            commonUrlParams.put(key, value);
            return this;
        }

        public Builder addCommonUrlParams(LinkedHashMap<String, String> params) {
            commonUrlParams = HttpUtil.mergeMap(commonUrlParams, params);
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
            commonHeaders = HttpUtil.mergeMap(commonUrlParams, headers);
            return this;
        }

        public Builder setDebug(String tag, HttpLogInterceptor.LogLevel logLevel) {
            HttpLogInterceptor logInterceptor = new HttpLogInterceptor(tag, logLevel);
            okHttpClientBuilder.addInterceptor(logInterceptor);
            return this;
        }

        public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        public Builder setSSLFactory(SSLFactory sslFactory) {
            if (sslFactory != null) {
                okHttpClientBuilder.sslSocketFactory(sslFactory.getsSLSocketFactory(), sslFactory.getTrustManager());
            }
            return this;
        }

        public Builder setCookieJar(CookieJar cookieJar) {
            okHttpClientBuilder.cookieJar(cookieJar);
            return this;
        }

        public CookieJar getCookieJar() {
            return getOkHttpClient().cookieJar();
        }

        public Builder addInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addNetworkInterceptor(interceptor);
            return this;
        }

        public LinkedHashMap<String, String> getCommonUrlParams() {
            return commonUrlParams;
        }

        public LinkedHashMap<String, String> getCommonHeaders() {
            return commonHeaders;
        }

        public OkHttpClient.Builder getOkHttpClientBuilder() {
            return okHttpClientBuilder;
        }

        public OkHttpClient getOkHttpClient() {
            if (okHttpClient == null) {
                okHttpClient = okHttpClientBuilder.build();
            }
            return okHttpClient;
        }

        public Builder setOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public Builder setRetryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Handler getMainScheduler() {
            return mainScheduler;
        }

        public Builder setMainScheduler(Handler mainScheduler) {
            this.mainScheduler = mainScheduler;
            return this;
        }

        public int getRefreshInterval() {
            return refreshInterval;
        }

        public Builder setRefreshInterval(int refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }
    }
}
