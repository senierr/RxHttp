package com.senierr.http;

import com.senierr.http.cookie.ClearableCookieJar;
import com.senierr.http.https.SSLFactory;
import com.senierr.http.internal.RequestFactory;
import com.senierr.http.model.HttpMethod;
import com.senierr.http.util.HttpLogInterceptor;
import com.senierr.http.util.Utils;

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
    public static final int DEFAULT_TIMEOUT = 30 * 1000;

    // 公共请求参数
    private LinkedHashMap<String, String> commonUrlParams;
    // 公共请求头
    private LinkedHashMap<String, String> commonHeaders;
    // Cookie管理器
    private ClearableCookieJar cookieJar;
    // 网络请求器
    private OkHttpClient okHttpClient;

    private RxHttp(Builder builder) {
        this.commonUrlParams = builder.commonUrlParams;
        this.commonHeaders = builder.commonHeaders;
        this.cookieJar = builder.cookieJar;
        okHttpClient = builder.okHttpClientBuilder.build();
    }

    /** get请求 */
    public RequestFactory get(String urlStr) {
        return method(HttpMethod.GET, urlStr);
    }

    /** post请求 */
    public RequestFactory post(String urlStr) {
        return method(HttpMethod.POST, urlStr);
    }

    /** head请求 */
    public RequestFactory head(String urlStr) {
        return method(HttpMethod.HEAD, urlStr);
    }

    /** delete请求 */
    public RequestFactory delete(String urlStr) {
        return method(HttpMethod.DELETE, urlStr);
    }

    /** put请求 */
    public RequestFactory put(String urlStr) {
        return method(HttpMethod.PUT, urlStr);
    }

    /** options请求 */
    public RequestFactory options(String urlStr) {
        return method(HttpMethod.OPTIONS, urlStr);
    }

    /** trace请求 */
    public RequestFactory trace(String urlStr) {
        return method(HttpMethod.TRACE, urlStr);
    }

    /** 自定义请求 */
    public RequestFactory method(HttpMethod method, String urlStr) {
        return new RequestFactory(this, method.toString(), urlStr);
    }

    public LinkedHashMap<String, String> getCommonUrlParams() {
        return commonUrlParams;
    }

    public LinkedHashMap<String, String> getCommonHeaders() {
        return commonHeaders;
    }

    public ClearableCookieJar getCookieJar() {
        return cookieJar;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public final static class Builder {
        private LinkedHashMap<String, String> commonUrlParams;
        private LinkedHashMap<String, String> commonHeaders;
        private ClearableCookieJar cookieJar;
        private OkHttpClient.Builder okHttpClientBuilder;

        public Builder() {
            okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            okHttpClientBuilder.retryOnConnectionFailure(true);
        }

        public RxHttp build() {
            return new RxHttp(this);
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

        /** okHttpClientBuilder配置 **/
        public Builder debug(String tag, HttpLogInterceptor.LogLevel logLevel) {
            HttpLogInterceptor logInterceptor = new HttpLogInterceptor(tag, logLevel);
            okHttpClientBuilder.addInterceptor(logInterceptor);
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
            this.cookieJar = cookieJar;
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
