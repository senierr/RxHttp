package com.senierr.sehttp.request;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.emitter.Emitter;
import com.senierr.sehttp.model.FileMap;
import com.senierr.sehttp.model.HttpHeaders;
import com.senierr.sehttp.model.HttpUrlParams;
import com.senierr.sehttp.util.HttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;

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
    private LinkedHashMap<String, String> httpUrlParams;
    // 请求头
    private LinkedHashMap<String, String> httpHeaders;
    // 请求体
    private RequestBodyWrapper requestBodyWrapper;

    public RequestBuilder(String method, String url) {
        this.method = method;
        this.url = url;
        requestBodyWrapper = new RequestBodyWrapper();
    }

    /**
     * 创建请求
     *
     * @return
     */
    public Request build(BaseCallback callback) {
        Request.Builder builder = new Request.Builder();
        httpUrlParams = HttpUtil.appendStringMap(httpUrlParams, SeHttp.getInstance().getCommonUrlParams());
        httpHeaders = HttpUtil.appendStringMap(httpHeaders, SeHttp.getInstance().getCommonHeaders());

        if (httpUrlParams != null && !httpUrlParams.isEmpty()) {
            url = HttpUrlParams.buildParams(url, httpUrlParams);
        }
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            builder.headers(HttpHeaders.buildHeaders(httpHeaders));
        }
        if (tag != null) {
            builder.tag(tag);
        }

        builder.method(method, requestBodyWrapper.build(callback));
        builder.url(url);
        return builder.build();
    }

    /**
     * 执行异步请求
     *
     * @param callback
     */
    public <T> void execute(BaseCallback<T> callback) {
        new Emitter<T>(this).execute(callback);
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
    public RequestBuilder addUrlParam(String key, String value) {
        if (httpUrlParams == null) {
            httpUrlParams = new LinkedHashMap<>();
        }
        httpUrlParams.put(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     *
     * @param params
     * @return
     */
    public RequestBuilder addUrlParams(LinkedHashMap<String, String> params) {
        httpUrlParams = HttpUtil.appendStringMap(httpUrlParams, params);
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
            httpHeaders = new LinkedHashMap<>();
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
    public RequestBuilder addHeaders(LinkedHashMap<String, String> headers) {
        httpHeaders = HttpUtil.appendStringMap(httpHeaders, headers);
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody
     * @return
     */
    public RequestBuilder requestBody(RequestBody requestBody) {
        requestBodyWrapper.setRequestBody(requestBody);
        return this;
    }

    public RequestBuilder requestBody(MediaType contentType, File file) {
        requestBodyWrapper.setRequestBody(RequestBody.create(contentType, file));
        return this;
    }

    public RequestBuilder requestBody(MediaType contentType, byte[] content, int offset, int byteCount) {
        requestBodyWrapper.setRequestBody(RequestBody.create(contentType, content, offset, byteCount));
        return this;
    }

    public RequestBuilder requestBody(MediaType contentType, byte[] content) {
        requestBodyWrapper.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestBuilder requestBody(MediaType contentType, ByteString content) {
        requestBodyWrapper.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestBuilder requestBody(MediaType contentType, String content) {
        requestBodyWrapper.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param key
     * @param file
     * @return
     */
    public RequestBuilder addRequestParam(String key, File file) {
        FileMap fileParams = requestBodyWrapper.getFileParams();
        if (fileParams == null) {
            fileParams = new FileMap();
        }
        fileParams.add(key, file);
        requestBodyWrapper.setFileParams(fileParams);
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param fileParams
     * @returns
     */
    public RequestBuilder addRequestFileParams(FileMap fileParams) {
        requestBodyWrapper.setFileParams(HttpUtil.appendFileMap(requestBodyWrapper.getFileParams(), fileParams));
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder addRequestParam(String key, String value) {
        LinkedHashMap<String, String> stringParams = requestBodyWrapper.getStringParams();
        if (stringParams == null) {
            stringParams = new LinkedHashMap<>();
        }
        stringParams.put(key, value);
        requestBodyWrapper.setStringParams(stringParams);
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param stringParams
     * @returns
     */
    public RequestBuilder addRequestStringParams(LinkedHashMap<String, String> stringParams) {
        requestBodyWrapper.setStringParams(HttpUtil.appendStringMap(requestBodyWrapper.getStringParams(), stringParams));
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestBuilder requestBody4JSon(String jsonStr) {
        requestBodyWrapper.setStringContent(jsonStr);
        requestBodyWrapper.setMediaType(MediaType.parse(RequestBodyWrapper.MEDIA_TYPE_JSON));
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public RequestBuilder requestBody4Text(String textStr) {
        requestBodyWrapper.setStringContent(textStr);
        requestBodyWrapper.setMediaType(MediaType.parse(RequestBodyWrapper.MEDIA_TYPE_PLAIN));
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public RequestBuilder requestBody4Xml(String xmlStr) {
        requestBodyWrapper.setStringContent(xmlStr);
        requestBodyWrapper.setMediaType(MediaType.parse(RequestBodyWrapper.MEDIA_TYPE_XML));
        return this;
    }

    /**
     * 设置字节流请求体
     *
     * @param bytes
     * @return
     */
    public RequestBuilder requestBody4Byte(byte[] bytes) {
        requestBodyWrapper.setBytes(bytes);
        requestBodyWrapper.setMediaType(MediaType.parse(RequestBodyWrapper.MEDIA_TYPE_STREAM));
        return this;
    }
}
