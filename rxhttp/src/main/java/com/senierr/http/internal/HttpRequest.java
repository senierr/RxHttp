package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;

import java.io.File;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 请求工厂类
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

    private @Nullable OnProgressListener onUploadListener;      // 上传进度监听
    private @Nullable OnProgressListener onDownloadListener;    // 下载进度监听

    private HttpRequest() {}

    /** 安全创建实例 */
    public static @NonNull HttpRequest newHttpRequest(@NonNull RxHttp rxHttp,
                                                      @NonNull HttpMethod httpMethod,
                                                      @NonNull String url) {
        HttpRequest httpRequest = new HttpRequest();
        httpRequest.rxHttp = rxHttp;
        httpRequest.httpMethod = httpMethod;
        httpRequest.httpUrl = new HttpUrl(url);
        httpRequest.httpHeaders = new HttpHeaders();
        httpRequest.httpRequestBody = new HttpRequestBody();
        // 添加公共URL参数
        httpRequest.addUrlParams(rxHttp.getCommonUrlParams());
        // 添加公共请求头
        httpRequest.addHeaders(rxHttp.getCommonHeaders());
        return httpRequest;
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public @NonNull HttpRequest addUrlParam(@NonNull String key, @NonNull String value) {
        httpUrl.addUrlParam(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public @NonNull HttpRequest addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        httpUrl.addUrlParams(params);
        return this;
    }

    /**
     * 添加头部
     *
     * @param key
     * @param value
     * @return
     */
    public @NonNull HttpRequest addHeader(@NonNull String key, @NonNull String value) {
        httpHeaders.addHeader(key, value);
        return this;
    }

    /**
     * 添加多个头部
     *
     * @param headers
     * @return
     */
    public @NonNull HttpRequest addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        httpHeaders.addHeaders(headers);
        return this;
    }

    /**
     * 添加文件参数
     *
     * @param key
     * @param file
     * @return
     */
    public @NonNull HttpRequest addRequestParam(@NonNull String key, @NonNull File file) {
        httpRequestBody.addRequestParam(key, file);
        return this;
    }

    /**
     * 添加多个文件参数
     *
     * @param fileParams
     * @returns
     */
    public @NonNull HttpRequest addRequestFileParams(@NonNull LinkedHashMap<String, File> fileParams) {
        httpRequestBody.addRequestFileParams(fileParams);
        return this;
    }

    /**
     * 添加字符串参数
     *
     * @param key
     * @param value
     * @return
     */
    public @NonNull HttpRequest addRequestParam(@NonNull String key, @NonNull String value) {
        httpRequestBody.addRequestParam(key, value);
        return this;
    }

    /**
     * 添加多个字符串参数
     *
     * @param stringParams
     * @returns
     */
    public @NonNull HttpRequest addRequestStringParams(@NonNull LinkedHashMap<String, String> stringParams) {
        httpRequestBody.addRequestStringParams(stringParams);
        return this;
    }

    /**
     * 设置是否分片上传
     *
     * @param isMultipart
     * @return
     */
    public @NonNull HttpRequest isMultipart(boolean isMultipart) {
        httpRequestBody.isMultipart(isMultipart);
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public @NonNull HttpRequest setRequestBody4JSon(@NonNull String jsonStr) {
        httpRequestBody.setRequestBody4JSon(jsonStr);
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public @NonNull HttpRequest setRequestBody4Text(@NonNull String textStr) {
        httpRequestBody.setRequestBody4Text(textStr);
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public @NonNull HttpRequest setRequestBody4Xml(@NonNull String xmlStr) {
        httpRequestBody.setRequestBody4Xml(xmlStr);
        return this;
    }

    /**
     * 设置字节流请求体
     *
     * @param bytes
     * @return
     */
    public @NonNull HttpRequest setRequestBody4Byte(@NonNull byte[] bytes) {
        httpRequestBody.setRequestBody4Byte(bytes);
        return this;
    }

    /**
     * 设置文件请求体
     *
     * @param file
     * @return
     */
    public @NonNull HttpRequest setRequestBody4File(@NonNull File file) {
        httpRequestBody.setRequestBody4File(file);
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody
     * @return
     */
    public @NonNull HttpRequest setRequestBody(@NonNull RequestBody requestBody) {
        httpRequestBody.setRequestBody(requestBody);
        return this;
    }

    /**
     * 设置上传进度监听
     */
    public @NonNull HttpRequest setOnUploadListener(OnProgressListener onUploadListener) {
        this.onUploadListener = onUploadListener;
        return this;
    }

    /**
     * 设置下载进度监听
     */
    public @NonNull HttpRequest setOnDownloadListener(OnProgressListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
        return this;
    }

    /** 创建请求 */
    public @NonNull Request generateRequest() {
        return generateRequest(null, null);
    }

    /** 执行请求 */
    public @NonNull <T> Observable<Response<T>> execute(@NonNull Converter<T> converter) {
        return new ExecuteObservable<>(rxHttp, this, converter, onUploadListener, onDownloadListener);
    }

    /** 创建请求 */
    @NonNull Request generateRequest(@Nullable OnProgressListener onUploadListener, @Nullable Disposable disposable) {
        Request.Builder requestBuilder = new Request.Builder();
        // 封装RequestBody
        RequestBody requestBody = httpRequestBody.generateRequestBody();
        if (requestBody != null && disposable != null && onUploadListener != null) {
            requestBody = new ProgressRequestBody(requestBody, onUploadListener, disposable);
        }
        requestBuilder.method(httpMethod.value(), requestBody);
        // 封装URL
        requestBuilder.url(httpUrl.generateUrl());
        // 封装Header
        requestBuilder.headers(httpHeaders.generateHeaders());
        return requestBuilder.build();
    }
}