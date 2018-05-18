package com.senierr.sehttp;

import com.senierr.sehttp.internal.RequestBuilder;
import com.senierr.sehttp.util.HttpLogInterceptor;
import com.senierr.sehttp.util.SSLParam;

import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * SeHttp入口
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class SeHttp {

    // 默认刷新时间
    public static final int REFRESH_MIN_INTERVAL = 100;
    // 构造器
    private Builder builder;

    private SeHttp(Builder builder) {
        this.builder = builder;
    }

    /**
     * get请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder get(String urlStr) {
        return method("GET", urlStr);
    }

    /**
     * post请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder post(String urlStr) {
        return method("POST", urlStr);
    }

    /**
     * head请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder head(String urlStr) {
        return method("HEAD", urlStr);
    }

    /**
     * delete请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder delete(String urlStr) {
        return method("DELETE", urlStr);
    }

    /**
     * put请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder put(String urlStr) {
        return method("PUT", urlStr);
    }

    /**
     * options请求
     *
     * @param urlStr
     * @return
     */
    public RequestBuilder options(String urlStr) {
        return method("OPTIONS", urlStr);
    }

    /**
     * 自定义请求方法
     *
     * @param method
     * @param urlStr
     * @return
     */
    public RequestBuilder method(String method, String urlStr) {
        return new RequestBuilder(this, method, urlStr);
    }

    public Builder getBuilder() {
        return builder;
    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }

    /**
     * SeHttp构造器
     *
     * @author zhouchunjie
     * @date 2018/5/17
     */
    public final static class Builder {

        // 默认超时时间
        private static final int DEFAULT_TIMEOUT = 30 * 1000;

        // 公共请求参数
        private LinkedHashMap<String, String> commonUrlParams;
        // 公共请求头
        private LinkedHashMap<String, String> commonHeaders;
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
        }

        /**
         * 构建SeHttp
         *
         * @return
         */
        public SeHttp build() {
            return new SeHttp(this);
        }

        /**
         * 设置连接超时
         *
         * @param connectTimeout
         * @return
         */
        public Builder setConnectTimeout(long connectTimeout) {
            okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置读超时
         *
         * @param readTimeout
         * @return
         */
        public Builder setReadTimeout(long readTimeout) {
            okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 设置写超时
         *
         * @param writeTimeout
         * @return
         */
        public Builder setWriteTimeout(long writeTimeout) {
            okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS);
            return this;
        }

        /**
         * 添加单个公共请求参数
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addCommonUrlParam(String key, String value) {
            if (commonUrlParams == null) {
                commonUrlParams = new LinkedHashMap<>();
            }
            commonUrlParams.put(key, value);
            return this;
        }

        /**
         * 添加单个公共头部
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addCommonHeader(String key, String value) {
            if (commonHeaders == null) {
                commonHeaders = new LinkedHashMap<>();
            }
            commonHeaders.put(key, value);
            return this;
        }

        /**
         * 开启Debug
         *
         * @param tag
         * @param logLevel
         * @return
         */
        public Builder setDebug(String tag, HttpLogInterceptor.LogLevel logLevel) {
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
        public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            okHttpClientBuilder.hostnameVerifier(hostnameVerifier);
            return this;
        }

        /**
         * 自定义SSL验证方式
         *
         * @param sslParams
         * @return
         */
        public Builder setSSLSocketFactory(SSLParam sslParams) {
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
        public Builder setCookieJar(CookieJar cookieJar) {
            okHttpClientBuilder.cookieJar(cookieJar);
            return this;
        }

        /**
         * 添加拦截器
         *
         * @param interceptor
         * @return
         */
        public Builder addInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * 添加网络拦截器
         *
         * @param interceptor
         * @return
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            okHttpClientBuilder.addNetworkInterceptor(interceptor);
            return this;
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
         * 获取网络请求器
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
         * 设置网络请求器
         *
         * @param okHttpClient
         */
        public void setOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
        }
    }
}
