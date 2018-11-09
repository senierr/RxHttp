package com.senierr.http.internal;

import android.support.annotation.NonNull;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;

import java.io.File;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * HTTP请求类
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class HttpRequest {

    private @NonNull RxHttp rxHttp;
    private @NonNull HttpMethod httpMethod;                  // 请求方法
    private @NonNull HttpUrl httpUrl;                        // 请求URL
    private @NonNull HttpHeaders httpHeaders;                // 请求头
    private @NonNull HttpRequestBody httpRequestBody;        // 请求体

    private HttpRequest(@NonNull RxHttp rxHttp,
                       @NonNull HttpMethod httpMethod,
                       @NonNull HttpUrl httpUrl) {
        this.rxHttp = rxHttp;
        this.httpMethod = httpMethod;
        this.httpUrl = httpUrl;
        this.httpHeaders = new HttpHeaders();
        this.httpRequestBody = new HttpRequestBody();
    }

    /** 安全创建实例 */
    public static @NonNull HttpRequest newHttpRequest(@NonNull RxHttp rxHttp,
                                                      @NonNull HttpMethod httpMethod,
                                                      @NonNull String url) {
        HttpRequest httpRequest = new HttpRequest(rxHttp, httpMethod, new HttpUrl(url));
        // 添加公共URL参数
        httpRequest.addUrlParams(rxHttp.getBaseUrlParams());
        // 添加公共请求头
        httpRequest.addHeaders(rxHttp.getBaseHeaders());
        return httpRequest;
    }

    /**
     * 添加请求参数
     */
    public @NonNull HttpRequest addUrlParam(@NonNull String key, @NonNull String value) {
        httpUrl.addUrlParam(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     */
    public @NonNull HttpRequest addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        httpUrl.addUrlParams(params);
        return this;
    }

    /**
     * 添加头部
     */
    public @NonNull HttpRequest addHeader(@NonNull String key, @NonNull String value) {
        httpHeaders.addHeader(key, value);
        return this;
    }

    /**
     * 添加多个头部
     */
    public @NonNull HttpRequest addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        httpHeaders.addHeaders(headers);
        return this;
    }

    /**
     * 添加文件参数
     */
    public @NonNull HttpRequest addRequestParam(@NonNull String key, @NonNull File file) {
        httpRequestBody.addRequestParam(key, file);
        return this;
    }

    /**
     * 添加多个文件参数
     */
    public @NonNull HttpRequest addRequestFileParams(@NonNull LinkedHashMap<String, File> fileParams) {
        httpRequestBody.addRequestFileParams(fileParams);
        return this;
    }

    /**
     * 添加字符串参数
     */
    public @NonNull HttpRequest addRequestParam(@NonNull String key, @NonNull String value) {
        httpRequestBody.addRequestParam(key, value);
        return this;
    }

    /**
     * 添加多个字符串参数
     */
    public @NonNull HttpRequest addRequestStringParams(@NonNull LinkedHashMap<String, String> stringParams) {
        httpRequestBody.addRequestStringParams(stringParams);
        return this;
    }

    /**
     * 设置是否分片上传
     */
    public @NonNull HttpRequest isMultipart(boolean isMultipart) {
        httpRequestBody.isMultipart(isMultipart);
        return this;
    }

    /**
     * 设置JSON格式请求体
     */
    public @NonNull HttpRequest setRequestBody4JSon(@NonNull String jsonStr) {
        httpRequestBody.setRequestBody4JSon(jsonStr);
        return this;
    }

    /**
     * 设置文本格式请求体
     */
    public @NonNull HttpRequest setRequestBody4Text(@NonNull String textStr) {
        httpRequestBody.setRequestBody4Text(textStr);
        return this;
    }

    /**
     * 设置XML格式请求体
     */
    public @NonNull HttpRequest setRequestBody4Xml(@NonNull String xmlStr) {
        httpRequestBody.setRequestBody4Xml(xmlStr);
        return this;
    }

    /**
     * 设置字节流请求体
     */
    public @NonNull HttpRequest setRequestBody4Byte(@NonNull byte[] bytes) {
        httpRequestBody.setRequestBody4Byte(bytes);
        return this;
    }

    /**
     * 设置文件请求体
     */
    public @NonNull HttpRequest setRequestBody4File(@NonNull File file) {
        httpRequestBody.setRequestBody4File(file);
        return this;
    }

    /**
     * 设置请求体
     */
    public @NonNull HttpRequest setRequestBody(@NonNull RequestBody requestBody) {
        httpRequestBody.setRequestBody(requestBody);
        return this;
    }

    /** 创建OkHttp请求 */
    public @NonNull Request generateRequest() {
        Request.Builder requestBuilder = new Request.Builder();
        // 封装method
        RequestBody requestBody = httpRequestBody.generateRequestBody();
        requestBuilder.method(httpMethod.value(), requestBody);
        // 封装URL
        requestBuilder.url(httpUrl.generateUrl());
        // 封装Header
        requestBuilder.headers(httpHeaders.generateHeaders());
        return requestBuilder.build();
    }

    /** 执行请求 */
    public @NonNull <T> Observable<Response<T>> execute(@NonNull Converter<T> converter) {
        return new ExecuteObservable<>(rxHttp, this, converter);
    }
}