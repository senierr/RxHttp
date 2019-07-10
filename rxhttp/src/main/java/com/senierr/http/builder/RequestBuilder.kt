package com.senierr.http.builder

import com.senierr.http.converter.Converter
import com.senierr.http.observable.RealObservable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File

/**
 * HTTP请求构建器
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
class RequestBuilder(
        // 请求方法构建器
        private val methodBuilder: MethodBuilder,
        // 请求行构建器
        private val urlBuilder: UrlBuilder,
        // 请求头构建器
        private val headerBuilder: HeaderBuilder,
        // 请求体构建器
        private val requestBodyBuilder: RequestBodyBuilder,
        // 网络请求器
        private val okHttpClient: OkHttpClient
) : Builder<Request> {

    private var uploadTag: String? = null
    private var downloadTag: String? = null

    /** 忽略基础请求地址  */
    fun ignoreBaseUrl(): RequestBuilder {
        urlBuilder.ignoreBaseUrl()
        return this
    }

    /** 忽略基础请求参数  */
    fun ignoreBaseUrlParams(): RequestBuilder {
        urlBuilder.ignoreBaseUrlParams()
        return this
    }

    /** 忽略基础请求头  */
    fun ignoreBaseHeaders(): RequestBuilder {
        headerBuilder.ignoreBaseHeaders()
        return this
    }

    /** 添加请求参数  */
    fun addUrlParam(key: String, value: String): RequestBuilder {
        urlBuilder.addUrlParam(key, value)
        return this
    }

    /** 添加多个请求参数  */
    fun addUrlParams(params: LinkedHashMap<String, String>): RequestBuilder {
        urlBuilder.addUrlParams(params)
        return this
    }

    /** 添加头部  */
    fun addHeader(key: String, value: String): RequestBuilder {
        headerBuilder.addHeader(key, value)
        return this
    }

    /** 添加多个头部  */
    fun addHeaders(headers: LinkedHashMap<String, String>): RequestBuilder {
        headerBuilder.addHeaders(headers)
        return this
    }

    /** 添加文件参数  */
    fun addRequestParam(key: String, file: File): RequestBuilder {
        requestBodyBuilder.addRequestParam(key, file)
        return this
    }

    /** 添加多个文件参数  */
    fun addRequestFileParams(fileParams: LinkedHashMap<String, File>): RequestBuilder {
        requestBodyBuilder.addRequestFileParams(fileParams)
        return this
    }

    /** 添加字符串参数  */
    fun addRequestParam(key: String, value: String): RequestBuilder {
        requestBodyBuilder.addRequestParam(key, value)
        return this
    }

    /** 添加多个字符串参数  */
    fun addRequestStringParams(stringParams: LinkedHashMap<String, String>): RequestBuilder {
        requestBodyBuilder.addRequestStringParams(stringParams)
        return this
    }

    /** 设置是否分片上传  */
    fun isMultipart(isMultipart: Boolean): RequestBuilder {
        requestBodyBuilder.isMultipart(isMultipart)
        return this
    }

    /** 设置JSON格式请求体  */
    fun requestBody4JSon(jsonStr: String): RequestBuilder {
        requestBodyBuilder.setRequestBody4JSon(jsonStr)
        return this
    }

    /** 设置文本格式请求体  */
    fun requestBody4Text(textStr: String): RequestBuilder {
        requestBodyBuilder.setRequestBody4Text(textStr)
        return this
    }

    /** 设置XML格式请求体  */
    fun requestBody4Xml(xmlStr: String): RequestBuilder {
        requestBodyBuilder.setRequestBody4Xml(xmlStr)
        return this
    }

    /** 设置字节流请求体  */
    fun requestBody4Byte(bytes: ByteArray): RequestBuilder {
        requestBodyBuilder.setRequestBody4Byte(bytes)
        return this
    }

    /** 设置文件请求体  */
    fun requestBody4File(file: File): RequestBuilder {
        requestBodyBuilder.setRequestBody4File(file)
        return this
    }

    /** 设置请求体  */
    fun requestBody(requestBody: RequestBody): RequestBuilder {
        requestBodyBuilder.setRequestBody(requestBody)
        return this
    }

    /**
     * 上传标签
     */
    fun uploadTag(tag: String): RequestBuilder {
        uploadTag = tag
        return this
    }

    /**
     * 下载标签
     */
    fun downloadTag(tag: String): RequestBuilder {
        downloadTag = tag
        return this
    }

    /** 创建OkHttp请求  */
    override fun build(): Request {
        return Request.Builder()
                .method(methodBuilder.build(), requestBodyBuilder.build())
                .url(urlBuilder.build())
                .headers(headerBuilder.build())
                .build()
    }

    /** 转换为被观察者  */
    fun <T> toObservable(converter: Converter<T>): Observable<T> {
        return RealObservable(okHttpClient, build(), uploadTag, downloadTag, converter)
                .onTerminateDetach()
    }
}