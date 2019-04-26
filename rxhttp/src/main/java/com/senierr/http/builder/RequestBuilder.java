package com.senierr.http.builder;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;
import com.senierr.http.model.ProgressResponse;
import com.senierr.http.observable.ProgressObservable;
import com.senierr.http.observable.ResultObservable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP请求构建器
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class RequestBuilder<T> {

    private @NonNull RxHttp rxHttp;

    private @NonNull MethodBuilder methodBuilder;           // 请求方法构建器
    private @NonNull UrlBuilder urlBuilder;                 // 请求行构建器
    private @NonNull HeaderBuilder headerBuilder;           // 请求头构建器
    private @NonNull RequestBodyBuilder requestBodyBuilder; // 请求体构建器

    private @NonNull Converter<T> converter;

    public RequestBuilder(@NonNull RxHttp rxHttp, @NonNull String method, @NonNull String url) {
        this.rxHttp = rxHttp;
        this.methodBuilder = new MethodBuilder(method);
        this.urlBuilder = new UrlBuilder(url);
        this.headerBuilder = new HeaderBuilder();
        this.requestBodyBuilder = new RequestBodyBuilder();
        // 设置基础请求地址
        setBaseUrl(rxHttp.getBaseUrl());
        // 添加公共URL参数
        addUrlParams(rxHttp.getBaseUrlParams());
        // 添加公共请求头
        addHeaders(rxHttp.getBaseHeaders());
    }

    /** 设置基础请求地址 */
    public @NonNull RequestBuilder<T> setBaseUrl(@Nullable String baseUrl) {
        urlBuilder.setBaseUrl(baseUrl);
        return this;
    }

    /** 忽略基础请求 */
    public @NonNull RequestBuilder<T> ignoreBaseUrl() {
        urlBuilder.setIgnoreBaseUrl(true);
        return this;
    }

    /** 添加请求参数 */
    public @NonNull RequestBuilder<T> addUrlParam(@NonNull String key, @NonNull String value) {
        urlBuilder.addUrlParam(key, value);
        return this;
    }

    /** 添加多个请求参数 */
    public @NonNull RequestBuilder<T> addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        urlBuilder.addUrlParams(params);
        return this;
    }

    /** 添加头部 */
    public @NonNull RequestBuilder<T> addHeader(@NonNull String key, @NonNull String value) {
        headerBuilder.addHeader(key, value);
        return this;
    }

    /** 添加多个头部 */
    public @NonNull RequestBuilder<T> addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        headerBuilder.addHeaders(headers);
        return this;
    }

    /** 添加文件参数 */
    public @NonNull RequestBuilder<T> addRequestParam(@NonNull String key, @NonNull File file) {
        requestBodyBuilder.addRequestParam(key, file);
        return this;
    }

    /** 添加多个文件参数 */
    public @NonNull RequestBuilder<T> addRequestFileParams(@NonNull LinkedHashMap<String, File> fileParams) {
        requestBodyBuilder.addRequestFileParams(fileParams);
        return this;
    }

    /** 添加字符串参数 */
    public @NonNull RequestBuilder<T> addRequestParam(@NonNull String key, @NonNull String value) {
        requestBodyBuilder.addRequestParam(key, value);
        return this;
    }

    /** 添加多个字符串参数 */
    public @NonNull RequestBuilder<T> addRequestStringParams(@NonNull LinkedHashMap<String, String> stringParams) {
        requestBodyBuilder.addRequestStringParams(stringParams);
        return this;
    }

    /** 设置是否分片上传 */
    public @NonNull RequestBuilder<T> isMultipart(boolean isMultipart) {
        requestBodyBuilder.isMultipart(isMultipart);
        return this;
    }

    /** 设置JSON格式请求体 */
    public @NonNull RequestBuilder<T> setRequestBody4JSon(@NonNull String jsonStr) {
        requestBodyBuilder.setRequestBody4JSon(jsonStr);
        return this;
    }

    /** 设置文本格式请求体 */
    public @NonNull RequestBuilder<T> setRequestBody4Text(@NonNull String textStr) {
        requestBodyBuilder.setRequestBody4Text(textStr);
        return this;
    }

    /** 设置XML格式请求体 */
    public @NonNull RequestBuilder<T> setRequestBody4Xml(@NonNull String xmlStr) {
        requestBodyBuilder.setRequestBody4Xml(xmlStr);
        return this;
    }

    /** 设置字节流请求体 */
    public @NonNull RequestBuilder<T> setRequestBody4Byte(@NonNull byte[] bytes) {
        requestBodyBuilder.setRequestBody4Byte(bytes);
        return this;
    }

    /** 设置文件请求体 */
    public @NonNull RequestBuilder<T> setRequestBody4File(@NonNull File file) {
        requestBodyBuilder.setRequestBody4File(file);
        return this;
    }

    /** 设置请求体 */
    public @NonNull RequestBuilder<T> setRequestBody(@NonNull RequestBody requestBody) {
        requestBodyBuilder.setRequestBody(requestBody);
        return this;
    }

    public @NonNull RequestBuilder<T> addConverter(@NonNull Converter<T> converter) {
        this.converter = converter;
        return this;
    }

    /** 创建OkHttp请求 */
    public @NonNull Request build() {
        return new Request.Builder()
                .method(methodBuilder.build(), requestBodyBuilder.build())
                .url(urlBuilder.build())
                .headers(headerBuilder.build())
                .build();
    }

    /** 转换为带上传进度的被观察者 */
    public @NonNull Observable<ProgressResponse<T>> toUploadObservable() {
        return ProgressObservable.upload(rxHttp, build(), converter)
                .onTerminateDetach();
    }

    /** 转换为带下载进度的被观察者 */
    public @NonNull Observable<ProgressResponse<T>> toDownloadObservable() {
        return ProgressObservable.download(rxHttp, build(), converter)
                .onTerminateDetach();
    }

    /** 转换为普通被观察者 */
    public @NonNull Observable<T> toResultObservable() {
        return ResultObservable.result(rxHttp, build(), converter)
                .onTerminateDetach();
    }

    /** 执行请求 */
    public Response execute() throws IOException {
        Call call = rxHttp.getOkHttpClient().newCall(build());
        return call.execute();
    }
}