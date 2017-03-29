package com.senierr.sehttp.request;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.emitter.Emitter;
import com.senierr.sehttp.mode.HttpHeaders;
import com.senierr.sehttp.mode.HttpParams;
import com.senierr.sehttp.mode.HttpRequestBody;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.util.HttpUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 请求封装类
 *
 * @author zhouchunjie
 * @date 2017/3/27
 */

public class RequestBuilder {

    // 请求方法
    private String method;
    // 请求
    private String url;
    // 标签
    private Object tag;
    // url参数
    private Map<String, String> httpParams;
    // 请求头
    private Map<String, String> httpHeaders;
    // 请求体
    private RequestBody requestBody;

    public RequestBuilder(String method, String url) {
        this.method = method;
        this.url = url;
    }

    /**
     * 创建请求
     *
     * @return
     */
    public Request build() {
        Request.Builder builder = new Request.Builder();
        // 添加公共参数
        httpParams = HttpUtil.appendMap(httpParams, SeHttp.getInstance().getCommonParams());
        // 添加公共头
        httpHeaders = HttpUtil.appendMap(httpHeaders, SeHttp.getInstance().getCommonHeaders());
        if (httpParams != null && !httpParams.isEmpty()) {
            url = HttpParams.buildParams(url, httpParams);
        }
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            builder.headers(HttpHeaders.buildHeaders(httpHeaders));
        }
        if (tag != null) {
            builder.tag(tag);
        }
        builder.method(method, requestBody);
        builder.url(url);
        return builder.build();
    }

    /**
     * 执行异步请求
     *
     * @param callback
     */
    @SuppressWarnings("unchecked")
    public <T> void execute(BaseCallback<T> callback) {
        new Emitter(this).execute(callback);
    }

    /**
     * 执行同步请求
     *
     * @return
     * @throws IOException
     */
    public Response execute() throws IOException {
        return new Emitter(this).execute();
    }

    /**
     * 添加标签
     *
     * @param tag
     * @return
     */
    public RequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 添加单个请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder addParam(String key, String value) {
        if (httpParams == null) {
            httpParams = new HashMap<>();
        }
        httpParams.put(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public RequestBuilder addParams(Map<String, String> params) {
        httpParams = HttpUtil.appendMap(httpParams, params);
        return this;
    }

    /**
     * 添加单个头部
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder addHeader(String key, String value) {
        if (httpHeaders == null) {
            httpHeaders = new HashMap<>();
        }
        httpHeaders.put(key, value);
        return this;
    }

    /**
     * 添加多个头部
     *
     * @param headers
     * @return
     */
    public RequestBuilder addHeaders(Map<String, String> headers) {
        httpHeaders = HttpUtil.appendMap(httpHeaders, headers);
        return this;
    }

    /**
     * 设置键值对请求体
     *
     * @param bodyParams
     * @return
     */
    public RequestBuilder requestBody4Form(Map<String, String> bodyParams) {
        this.requestBody = HttpRequestBody.buildRequestBody4Form(bodyParams);
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestBuilder requestBody4JSon(String jsonStr) {
        this.requestBody = HttpRequestBody.buildRequestBody4Json(jsonStr);
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public RequestBuilder requestBody4Text(String textStr) {
        this.requestBody = HttpRequestBody.buildRequestBody4Text(textStr);
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public RequestBuilder requestBody4Xml(String xmlStr) {
        this.requestBody = HttpRequestBody.buildRequestBody4Xml(xmlStr);
        return this;
    }



}
