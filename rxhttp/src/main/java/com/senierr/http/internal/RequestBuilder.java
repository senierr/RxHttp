package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;
import com.senierr.http.converter.DefaultConverter;

import java.io.File;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * HTTP请求构建器
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class RequestBuilder {

    private @NonNull RxHttp rxHttp;

    private @NonNull MethodBuilder methodBuilder;           // 请求方法构建器
    private @NonNull UrlBuilder urlBuilder;                 // 请求行构建器
    private @NonNull HeaderBuilder headerBuilder;           // 请求头构建器
    private @NonNull RequestBodyBuilder requestBodyBuilder; // 请求体构建器

    private @Nullable OnProgressListener onUploadListener;
    private @Nullable OnProgressListener onDownloadListener;

    public RequestBuilder(@NonNull RxHttp rxHttp, @NonNull String method, @NonNull String url) {
        this.rxHttp = rxHttp;
        this.methodBuilder = new MethodBuilder(method);
        this.urlBuilder = new UrlBuilder(url);
        this.headerBuilder = new HeaderBuilder();
        this.requestBodyBuilder = new RequestBodyBuilder();
        // 添加公共URL参数
        addUrlParams(rxHttp.getBaseUrlParams());
        // 添加公共请求头
        addHeaders(rxHttp.getBaseHeaders());
    }

    /** 添加请求参数 */
    public @NonNull RequestBuilder addUrlParam(@NonNull String key, @NonNull String value) {
        urlBuilder.addUrlParam(key, value);
        return this;
    }

    /** 添加多个请求参数 */
    public @NonNull RequestBuilder addUrlParams(@NonNull LinkedHashMap<String, String> params) {
        urlBuilder.addUrlParams(params);
        return this;
    }

    /** 添加头部 */
    public @NonNull RequestBuilder addHeader(@NonNull String key, @NonNull String value) {
        headerBuilder.addHeader(key, value);
        return this;
    }

    /** 添加多个头部 */
    public @NonNull RequestBuilder addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        headerBuilder.addHeaders(headers);
        return this;
    }

    /** 添加文件参数 */
    public @NonNull RequestBuilder addRequestParam(@NonNull String key, @NonNull File file) {
        requestBodyBuilder.addRequestParam(key, file);
        return this;
    }

    /** 添加多个文件参数 */
    public @NonNull RequestBuilder addRequestFileParams(@NonNull LinkedHashMap<String, File> fileParams) {
        requestBodyBuilder.addRequestFileParams(fileParams);
        return this;
    }

    /** 添加字符串参数 */
    public @NonNull RequestBuilder addRequestParam(@NonNull String key, @NonNull String value) {
        requestBodyBuilder.addRequestParam(key, value);
        return this;
    }

    /** 添加多个字符串参数 */
    public @NonNull RequestBuilder addRequestStringParams(@NonNull LinkedHashMap<String, String> stringParams) {
        requestBodyBuilder.addRequestStringParams(stringParams);
        return this;
    }

    /** 设置是否分片上传 */
    public @NonNull RequestBuilder isMultipart(boolean isMultipart) {
        requestBodyBuilder.isMultipart(isMultipart);
        return this;
    }

    /** 设置JSON格式请求体 */
    public @NonNull RequestBuilder setRequestBody4JSon(@NonNull String jsonStr) {
        requestBodyBuilder.setRequestBody4JSon(jsonStr);
        return this;
    }

    /** 设置文本格式请求体 */
    public @NonNull RequestBuilder setRequestBody4Text(@NonNull String textStr) {
        requestBodyBuilder.setRequestBody4Text(textStr);
        return this;
    }

    /** 设置XML格式请求体 */
    public @NonNull RequestBuilder setRequestBody4Xml(@NonNull String xmlStr) {
        requestBodyBuilder.setRequestBody4Xml(xmlStr);
        return this;
    }

    /** 设置字节流请求体 */
    public @NonNull RequestBuilder setRequestBody4Byte(@NonNull byte[] bytes) {
        requestBodyBuilder.setRequestBody4Byte(bytes);
        return this;
    }

    /** 设置文件请求体 */
    public @NonNull RequestBuilder setRequestBody4File(@NonNull File file) {
        requestBodyBuilder.setRequestBody4File(file);
        return this;
    }

    /** 设置请求体 */
    public @NonNull RequestBuilder setRequestBody(@NonNull RequestBody requestBody) {
        requestBodyBuilder.setRequestBody(requestBody);
        return this;
    }

    /** 设置上传进度监听 */
    public @NonNull RequestBuilder setOnUploadListener(@NonNull OnProgressListener onUploadListener) {
        this.onUploadListener = onUploadListener;
        return this;
    }

    /** 设置下载进度监听 */
    public @NonNull RequestBuilder setOnDownloadListener(@NonNull OnProgressListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
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

    /** 执行请求 */
    public @NonNull <T> Observable<T> execute(@NonNull Converter<T> converter) {
        return new ExecuteObservable<>(rxHttp, build(), converter)
                .setOnUploadListener(onUploadListener)
                .setOnDownloadListener(onDownloadListener)
                .onTerminateDetach();
    }

    /** 执行请求 */
    public @NonNull Observable<Response> execute() {
        return new ExecuteObservable<>(rxHttp, build(), new DefaultConverter())
                .setOnUploadListener(onUploadListener)
                .setOnDownloadListener(onDownloadListener)
                .onTerminateDetach();
    }
}