package com.senierr.http.builder;

import android.text.TextUtils;

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

    private boolean ignoreBaseUrl = false;
    private boolean ignoreBaseUrlParams = false;
    private boolean ignoreBaseHeaders = false;

    private @NonNull Converter<T> converter;

    public RequestBuilder(@NonNull RxHttp rxHttp, @NonNull String method, @NonNull String url) {
        this.rxHttp = rxHttp;
        this.methodBuilder = new MethodBuilder(method);
        this.urlBuilder = new UrlBuilder(url);
        this.headerBuilder = new HeaderBuilder();
        this.requestBodyBuilder = new RequestBodyBuilder();
    }

    /** 忽略基础请求地址 */
    public @NonNull RequestBuilder<T> ignoreBaseUrl() {
        ignoreBaseUrl = true;
        return this;
    }

    /** 忽略基础请求参数 */
    public @NonNull RequestBuilder<T> ignoreBaseUrlParams() {
        ignoreBaseUrlParams = true;
        return this;
    }

    /** 忽略基础请求头 */
    public @NonNull RequestBuilder<T> ignoreBaseHeaders() {
        ignoreBaseHeaders = true;
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
        // 若设置了基础请求地址，且未忽略，拼接基础请求地址
        String actualUrl = urlBuilder.getUrl();
        if (!TextUtils.isEmpty(rxHttp.getBaseUrl()) && !ignoreBaseUrl) {
            actualUrl = rxHttp.getBaseUrl() + urlBuilder.getUrl();
        }
        urlBuilder.setUrl(actualUrl);

        // 若设置了基础参数，且未忽略，添加基础参数（注意添加顺序）
        LinkedHashMap<String, String> actualUrlParams = new LinkedHashMap<>();
        if (!rxHttp.getBaseUrlParams().isEmpty() && !ignoreBaseUrlParams) {
            actualUrlParams.putAll(rxHttp.getBaseUrlParams());
        }
        actualUrlParams.putAll(urlBuilder.getUrlParams());
        urlBuilder.getUrlParams().clear();
        urlBuilder.addUrlParams(actualUrlParams);

        // 若设置了基础头，且未忽略，添加基础头（注意添加顺序）
        LinkedHashMap<String, String> actualHeaders = new LinkedHashMap<>();
        if (!rxHttp.getBaseHeaders().isEmpty() && !ignoreBaseHeaders) {
            actualHeaders.putAll(rxHttp.getBaseHeaders());
        }
        actualHeaders.putAll(headerBuilder.getHeaders());
        headerBuilder.getHeaders().clear();
        headerBuilder.addHeaders(actualHeaders);

        return new Request.Builder()
                .method(methodBuilder.build(), requestBodyBuilder.build())
                .url(urlBuilder.build())
                .headers(headerBuilder.build())
                .build();
    }

    /** 转换为带上传进度的被观察者 */
    public @NonNull Observable<ProgressResponse<T>> toUploadObservable() {
        return ProgressObservable.upload(rxHttp.getOkHttpClient(), build(), converter)
                .onTerminateDetach();
    }

    /** 转换为带下载进度的被观察者 */
    public @NonNull Observable<ProgressResponse<T>> toDownloadObservable() {
        return ProgressObservable.download(rxHttp.getOkHttpClient(), build(), converter)
                .onTerminateDetach();
    }

    /** 转换为普通被观察者 */
    public @NonNull Observable<T> toResultObservable() {
        return ResultObservable.result(rxHttp.getOkHttpClient(), build(), converter)
                .onTerminateDetach();
    }

    /** 执行请求 */
    public Response execute() throws IOException {
        Call call = rxHttp.getOkHttpClient().newCall(build());
        return call.execute();
    }
}