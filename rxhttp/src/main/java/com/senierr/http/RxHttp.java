package com.senierr.http;

import android.support.annotation.NonNull;

import com.senierr.http.internal.LogInterceptor;
import com.senierr.http.internal.MethodBuilder;
import com.senierr.http.internal.RequestBuilder;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * RxHttp
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class RxHttp {

    // 进度回调最小间隔时长(ms)
    public static final long REFRESH_MIN_INTERVAL = 100;

    // 基础请求参数
    private @NonNull LinkedHashMap<String, String> baseUrlParams;
    // 基础请求头
    private @NonNull LinkedHashMap<String, String> baseHeaders;
    // 网络请求器
    private @NonNull OkHttpClient okHttpClient;

    private RxHttp(Builder builder) {
        this.baseUrlParams = builder.baseUrlParams;
        this.baseHeaders = builder.baseHeaders;
        okHttpClient = builder.okHttpClientBuilder.build();
    }

    /** get请求 */
    public @NonNull RequestBuilder get(@NonNull String urlStr) {
        return method(MethodBuilder.GET, urlStr);
    }

    /** post请求 */
    public @NonNull RequestBuilder post(@NonNull String urlStr) {
        return method(MethodBuilder.POST, urlStr);
    }

    /** head请求 */
    public @NonNull RequestBuilder head(@NonNull String urlStr) {
        return method(MethodBuilder.HEAD, urlStr);
    }

    /** delete请求 */
    public @NonNull RequestBuilder delete(@NonNull String urlStr) {
        return method(MethodBuilder.DELETE, urlStr);
    }

    /** put请求 */
    public @NonNull RequestBuilder put(@NonNull String urlStr) {
        return method(MethodBuilder.PUT, urlStr);
    }

    /** patch请求 */
    public @NonNull RequestBuilder patch(@NonNull String urlStr) {
        return method(MethodBuilder.PATCH, urlStr);
    }

    /** options请求 */
    public @NonNull RequestBuilder options(@NonNull String urlStr) {
        return method(MethodBuilder.OPTIONS, urlStr);
    }

    /** trace请求 */
    public @NonNull RequestBuilder trace(@NonNull String urlStr) {
        return method(MethodBuilder.TRACE, urlStr);
    }

    /** 自定义请求 */
    public @NonNull RequestBuilder method(@NonNull String method, @NonNull String url) {
        return new RequestBuilder(this, method, url);
    }

    public @NonNull LinkedHashMap<String, String> getBaseUrlParams() {
        return baseUrlParams;
    }

    public @NonNull LinkedHashMap<String, String> getBaseHeaders() {
        return baseHeaders;
    }

    public @NonNull OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static final class Builder {
        private @NonNull LinkedHashMap<String, String> baseUrlParams = new LinkedHashMap<>();
        private @NonNull LinkedHashMap<String, String> baseHeaders = new LinkedHashMap<>();
        private @NonNull OkHttpClient.Builder okHttpClientBuilder;

        public Builder() {
            okHttpClientBuilder = new OkHttpClient.Builder();
        }

        public Builder(@NonNull OkHttpClient.Builder okHttpClientBuilder) {
            this.okHttpClientBuilder = okHttpClientBuilder;
        }

        public @NonNull RxHttp build() {
            return new RxHttp(this);
        }

        /** 自定义配置 */
        public @NonNull Builder addBaseUrlParam(@NonNull String key, @NonNull String value) {
            baseUrlParams.put(key, value);
            return this;
        }

        public @NonNull Builder addBaseUrlParams(@NonNull LinkedHashMap<String, String> params) {
            baseUrlParams.putAll(params);
            return this;
        }

        public @NonNull Builder addBaseHeader(@NonNull String key, @NonNull String value) {
            baseHeaders.put(key, value);
            return this;
        }

        public @NonNull Builder addBaseHeaders(@NonNull LinkedHashMap<String, String> headers) {
            baseHeaders.putAll(headers);
            return this;
        }

        /** OkHttp常用配置 */
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

        public @NonNull Builder sslSocketFactory(@NonNull SSLSocketFactory sslSocketFactory,
                                                 @NonNull X509TrustManager trustManager) {
            okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
            return this;
        }

        public @NonNull Builder cookieJar(@NonNull CookieJar cookieJar) {
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