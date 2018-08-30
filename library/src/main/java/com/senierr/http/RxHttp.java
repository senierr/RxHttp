package com.senierr.http;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.cookie.ClearableCookieJar;
import com.senierr.http.https.SSLFactory;
import com.senierr.http.internal.HttpRequest;
import com.senierr.http.internal.HttpMethod;
import com.senierr.http.internal.LogInterceptor;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * RxHttp
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class RxHttp {

    // 默认超时时间
    public static final long DEFAULT_TIMEOUT = 30 * 1000;
    // 进度回调最小间隔时长(ms)
    public static final long REFRESH_MIN_INTERVAL = 100;

    // 公共请求参数
    private @NonNull LinkedHashMap<String, String> commonUrlParams;
    // 公共请求头
    private @NonNull LinkedHashMap<String, String> commonHeaders;
    // Cookie管理器
    private @Nullable ClearableCookieJar cookieJar;
    // 网络请求器
    private @NonNull OkHttpClient okHttpClient;

    private RxHttp(Builder builder) {
        this.commonUrlParams = builder.commonUrlParams;
        this.commonHeaders = builder.commonHeaders;
        this.cookieJar = builder.cookieJar;
        okHttpClient = builder.okHttpClientBuilder.build();
    }

    /** get请求 */
    public @NonNull HttpRequest get(@NonNull String urlStr) {
        return method(HttpMethod.GET, urlStr);
    }

    /** post请求 */
    public @NonNull HttpRequest post(@NonNull String urlStr) {
        return method(HttpMethod.POST, urlStr);
    }

    /** head请求 */
    public @NonNull HttpRequest head(@NonNull String urlStr) {
        return method(HttpMethod.HEAD, urlStr);
    }

    /** delete请求 */
    public @NonNull HttpRequest delete(@NonNull String urlStr) {
        return method(HttpMethod.DELETE, urlStr);
    }

    /** put请求 */
    public @NonNull HttpRequest put(@NonNull String urlStr) {
        return method(HttpMethod.PUT, urlStr);
    }

    /** options请求 */
    public @NonNull HttpRequest options(@NonNull String urlStr) {
        return method(HttpMethod.OPTIONS, urlStr);
    }

    /** trace请求 */
    public @NonNull HttpRequest trace(@NonNull String urlStr) {
        return method(HttpMethod.TRACE, urlStr);
    }

    public @NonNull HttpRequest method(@NonNull HttpMethod method, @NonNull String urlStr) {
        HttpRequest httpRequest = HttpRequest.newHttpRequest(this, method, urlStr);
        // 添加公共URL参数
        httpRequest.addUrlParams(commonUrlParams);
        // 添加公共请求头
        httpRequest.addHeaders(commonHeaders);
        return httpRequest;
    }

    public @NonNull LinkedHashMap<String, String> getCommonUrlParams() {
        return commonUrlParams;
    }

    public @NonNull LinkedHashMap<String, String> getCommonHeaders() {
        return commonHeaders;
    }

    public @Nullable ClearableCookieJar getCookieJar() {
        return cookieJar;
    }

    public @NonNull OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public final static class Builder {
        private @NonNull LinkedHashMap<String, String> commonUrlParams;
        private @NonNull LinkedHashMap<String, String> commonHeaders;
        private @Nullable ClearableCookieJar cookieJar;
        private @NonNull OkHttpClient.Builder okHttpClientBuilder;

        public Builder() {
            commonUrlParams = new LinkedHashMap<>();
            commonHeaders = new LinkedHashMap<>();
            okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.retryOnConnectionFailure(true);
        }

        public @NonNull RxHttp build() {
            return new RxHttp(this);
        }

        /** 自定义配置 **/
        public @NonNull Builder addCommonUrlParam(@NonNull String key, @NonNull String value) {
            commonUrlParams.put(key, value);
            return this;
        }

        public @NonNull Builder addCommonUrlParams(@NonNull LinkedHashMap<String, String> params) {
            commonUrlParams.putAll(params);
            return this;
        }

        public @NonNull Builder addCommonHeader(@NonNull String key, @NonNull String value) {
            commonHeaders.put(key, value);
            return this;
        }

        public @NonNull Builder addCommonHeaders(@NonNull LinkedHashMap<String, String> headers) {
            commonHeaders.putAll(headers);
            return this;
        }

        /** okHttpClientBuilder配置 **/
        public @NonNull Builder debug(@NonNull String tag, @NonNull LogInterceptor.LogLevel logLevel) {
            LogInterceptor logInterceptor = new LogInterceptor(tag, logLevel);
            okHttpClientBuilder.addInterceptor(logInterceptor);
            return this;
        }

        public @NonNull Builder connectTimeout(long connectTimeout) {
            okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public @NonNull Builder readTimeout(long readTimeout) {
            okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public @NonNull Builder writeTimeout(long writeTimeout) {
            okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        public @NonNull Builder hostnameVerifier(@NonNull HostnameVerifier hostnameVerifier) {
            okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        public @NonNull Builder sslFactory(@NonNull SSLFactory sslFactory) {
            okHttpClientBuilder.sslSocketFactory(sslFactory.getsSLSocketFactory(), sslFactory.getTrustManager());
            return this;
        }

        public @NonNull Builder cookieJar(@NonNull ClearableCookieJar cookieJar) {
            this.cookieJar = cookieJar;
            okHttpClientBuilder.cookieJar(cookieJar);
            return this;
        }

        public @NonNull Builder addInterceptor(@NonNull Interceptor interceptor) {
            okHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        public @NonNull Builder addNetworkInterceptor(@NonNull Interceptor interceptor) {
            okHttpClientBuilder.addNetworkInterceptor(interceptor);
            return this;
        }
    }
}