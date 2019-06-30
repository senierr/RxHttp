package com.senierr.http

import com.senierr.http.builder.*
import com.senierr.http.interceptor.LogInterceptor
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * RxHttp
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
class RxHttp private constructor(
        // 基础请求地址
        private val baseUrl: String?,
        // 基础请求参数
        private val baseUrlParams: LinkedHashMap<String, String>,
        // 基础请求头
        private val baseHeaders: LinkedHashMap<String, String>,
        // 网络请求器
        private val okHttpClient: OkHttpClient
) {
    /** get请求  */
    fun get(urlStr: String): RequestBuilder {
        return method(MethodBuilder.GET, urlStr)
    }

    /** post请求  */
    fun post(urlStr: String): RequestBuilder {
        return method(MethodBuilder.POST, urlStr)
    }

    /** head请求  */
    fun head(urlStr: String): RequestBuilder {
        return method(MethodBuilder.HEAD, urlStr)
    }

    /** delete请求  */
    fun delete(urlStr: String): RequestBuilder {
        return method(MethodBuilder.DELETE, urlStr)
    }

    /** put请求  */
    fun put(urlStr: String): RequestBuilder {
        return method(MethodBuilder.PUT, urlStr)
    }

    /** patch请求  */
    fun patch(urlStr: String): RequestBuilder {
        return method(MethodBuilder.PATCH, urlStr)
    }

    /** options请求  */
    fun options(urlStr: String): RequestBuilder {
        return method(MethodBuilder.OPTIONS, urlStr)
    }

    /** trace请求  */
    fun trace(urlStr: String): RequestBuilder {
        return method(MethodBuilder.TRACE, urlStr)
    }

    /** 自定义请求  */
    fun method(method: String, url: String): RequestBuilder {
        val methodBuilder = MethodBuilder(method)
        val urlBuilder = UrlBuilder(url)
        urlBuilder.baseUrl(baseUrl)
        urlBuilder.addBaseUrlParams(baseUrlParams)
        val headerBuilder = HeaderBuilder()
        headerBuilder.addBaseHeaders(baseHeaders)
        val requestBodyBuilder = RequestBodyBuilder()
        return RequestBuilder(methodBuilder, urlBuilder, headerBuilder, requestBodyBuilder, okHttpClient)
    }

    class Builder(private val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()) {

        private var baseUrl: String? = null
        private val baseUrlParams = LinkedHashMap<String, String>()
        private val baseHeaders = LinkedHashMap<String, String>()

        /** 自定义配置  */
        fun baseUrl(baseUrl: String): Builder {
            this.baseUrl = baseUrl
            return this
        }

        fun addBaseUrlParam(key: String, value: String): Builder {
            baseUrlParams[key] = value
            return this
        }

        fun addBaseUrlParams(params: LinkedHashMap<String, String>): Builder {
            baseUrlParams.putAll(params)
            return this
        }

        fun addBaseHeader(key: String, value: String): Builder {
            baseHeaders[key] = value
            return this
        }

        fun addBaseHeaders(headers: LinkedHashMap<String, String>): Builder {
            baseHeaders.putAll(headers)
            return this
        }

        /** OkHttp常用配置  */
        fun debug(tag: String, logLevel: LogInterceptor.LogLevel): Builder {
            okHttpClientBuilder.addInterceptor(LogInterceptor(tag, logLevel))
            return this
        }

        fun connectTimeout(connectTimeout: Long): Builder {
            okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            return this
        }

        fun readTimeout(readTimeout: Long): Builder {
            okHttpClientBuilder.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
            return this
        }

        fun writeTimeout(writeTimeout: Long): Builder {
            okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
            return this
        }

        fun hostnameVerifier(hostnameVerifier: HostnameVerifier): Builder {
            okHttpClientBuilder.hostnameVerifier(hostnameVerifier)
            return this
        }

        fun sslSocketFactory(sslSocketFactory: SSLSocketFactory, trustManager: X509TrustManager): Builder {
            okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
            return this
        }

        fun cookieJar(cookieJar: CookieJar): Builder {
            okHttpClientBuilder.cookieJar(cookieJar)
            return this
        }

        fun build(): RxHttp {
            return RxHttp(baseUrl, baseUrlParams, baseHeaders, okHttpClientBuilder.build())
        }
    }
}