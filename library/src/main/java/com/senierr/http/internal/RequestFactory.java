package com.senierr.http.internal;

import com.senierr.http.RxHttp;
import com.senierr.http.listener.OnProgressListener;
import com.senierr.http.model.HttpHeaders;
import com.senierr.http.model.HttpMethod;
import com.senierr.http.model.HttpRequestBody;
import com.senierr.http.model.HttpUrl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 请求工厂类
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */
public final class RequestFactory {

    private RxHttp rxHttp;
    private HttpMethod httpMethod;                  // 请求方法
    private HttpUrl httpUrl;                        // 请求URL
    private HttpHeaders httpHeaders;                // 请求头
    private HttpRequestBody httpRequestBody;        // 请求体

    private OnProgressListener onUploadListener;    // 上传进度监听
    private OnProgressListener onDownloadListener;  // 下载进度监听

    private RequestFactory() {}

    /** 安全创建实例 */
    public static RequestFactory newRequestFactory(RxHttp rxHttp, HttpMethod httpMethod, HttpUrl httpUrl) {
        RequestFactory requestFactory = new RequestFactory();
        requestFactory.rxHttp = rxHttp;
        requestFactory.httpMethod = httpMethod;
        requestFactory.httpUrl = httpUrl;
        requestFactory.httpHeaders = new HttpHeaders();
        requestFactory.httpRequestBody = new HttpRequestBody();
        return requestFactory;
    }

    /** 创建请求 */
    public Request create() {
        Request.Builder requestBuilder = new Request.Builder();
        // 封装RequestBody
        RequestBody requestBody = httpRequestBody.generateRequestBody();
        if (requestBody != null) {
            requestBody = new ProgressRequestBody(requestBody, onUploadListener);
        }
        requestBuilder.method(httpMethod.value(), requestBody);
        // 封装URL
        requestBuilder.url(httpUrl.generateUrl());
        // 封装Header
        requestBuilder.headers(httpHeaders.generateHeaders());
        return requestBuilder.build();
    }

    /**
     * 执行同步请求
     *
     * @return
     * @throws IOException
     */
    public Response execute() throws IOException {
        Response response = rxHttp.getOkHttpClient().newCall(create()).execute();
        return response.newBuilder()
                .body(new ProgressResponseBody(response.body(), onDownloadListener))
                .build();
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestFactory addUrlParam(String key, String value) {
        httpUrl.addUrlParam(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public RequestFactory addUrlParams(LinkedHashMap<String, String> params) {
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
    public RequestFactory addHeader(String key, String value) {
        httpHeaders.addHeader(key, value);
        return this;
    }

    /**
     * 添加多个头部
     *
     * @param headers
     * @return
     */
    public RequestFactory addHeaders(LinkedHashMap<String, String> headers) {
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
    public RequestFactory addRequestParam(String key, File file) {
        httpRequestBody.addRequestParam(key, file);
        return this;
    }

    /**
     * 添加多个文件参数
     *
     * @param fileParams
     * @returns
     */
    public RequestFactory addRequestFileParams(LinkedHashMap<String, File> fileParams) {
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
    public RequestFactory addRequestParam(String key, String value) {
        httpRequestBody.addRequestParam(key, value);
        return this;
    }

    /**
     * 添加多个字符串参数
     *
     * @param stringParams
     * @returns
     */
    public RequestFactory addRequestStringParams(LinkedHashMap<String, String> stringParams) {
        httpRequestBody.addRequestStringParams(stringParams);
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestFactory setRequestBody4JSon(String jsonStr) {
        httpRequestBody.setRequestBody4JSon(jsonStr);
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public RequestFactory setRequestBody4Text(String textStr) {
        httpRequestBody.setRequestBody4Text(textStr);
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public RequestFactory setRequestBody4Xml(String xmlStr) {
        httpRequestBody.setRequestBody4Xml(xmlStr);
        return this;
    }

    /**
     * 设置字节流请求体
     *
     * @param bytes
     * @return
     */
    public RequestFactory setRequestBody4Byte(byte[] bytes) {
        httpRequestBody.setRequestBody4Byte(bytes);
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody
     * @return
     */
    public RequestFactory setRequestBody(RequestBody requestBody) {
        httpRequestBody.setRequestBody(requestBody);
        return this;
    }

    /**
     * 设置上传进度监听
     *
     * @param onUploadListener
     */
    public RequestFactory onUploadListener(OnProgressListener onUploadListener) {
        this.onUploadListener = onUploadListener;
        return this;
    }

    /**
     * 设置下载进度监听
     *
     * @param onDownloadListener
     */
    public RequestFactory onDownloadListener(OnProgressListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
        return this;
    }
}