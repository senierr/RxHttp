package com.senierr.sehttp.request;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.emitter.Emitter;
import com.senierr.sehttp.mode.FileMap;
import com.senierr.sehttp.mode.HttpHeaders;
import com.senierr.sehttp.mode.HttpParams;
import com.senierr.sehttp.mode.HttpRequestBody;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.util.HttpUtil;
import com.senierr.sehttp.util.SeLogger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.MediaType;
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
    private LinkedHashMap<String, String> httpParams;
    // 请求头
    private LinkedHashMap<String, String> httpHeaders;
    // 请求体
    private HttpRequestBody httpRequestBody;

    public RequestBuilder(String method, String url) {
        this.method = method;
        this.url = url;
        httpRequestBody = new HttpRequestBody();
    }

    /**
     * 创建请求
     *
     * @return
     */
    public Request build(BaseCallback callback) {
        Request.Builder builder = new Request.Builder();
        // 添加公共参数
        httpParams = HttpUtil.appendStringMap(httpParams, SeHttp.getInstance().getCommonParams());
        // 添加公共头
        httpHeaders = HttpUtil.appendStringMap(httpHeaders, SeHttp.getInstance().getCommonHeaders());
        if (httpParams != null && !httpParams.isEmpty()) {
            url = HttpParams.buildParams(url, httpParams);
        }

        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            builder.headers(HttpHeaders.buildHeaders(httpHeaders));
        }
        if (tag != null) {
            builder.tag(tag);
        }

        RequestBody requestBody = httpRequestBody.create(callback);
        try {
            if (requestBody != null) {
                long contentLength = requestBody.contentLength();
                if (contentLength > 0) {
                    addHeader("Content-Length", String.valueOf(contentLength));
                }
            }
        } catch (IOException e) {
            SeLogger.e(e);
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
    public RequestBuilder addUrlParam(String key, String value) {
        if (httpParams == null) {
            httpParams = new LinkedHashMap<>();
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
    public RequestBuilder addUrlParams(LinkedHashMap<String, String> params) {
        httpParams = HttpUtil.appendStringMap(httpParams, params);
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
        httpRequestBody.setRequestBody(requestBody);
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
        FileMap fileParams = httpRequestBody.getFileParams();
        if (fileParams == null) {
            fileParams = new FileMap();
        }
        fileParams.add(key, file);
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param fileParams
     * @returns
     */
    public RequestBuilder addRequestFileParams(FileMap fileParams) {
        httpRequestBody.setFileParams(HttpUtil.appendFileMap(httpRequestBody.getFileParams(), fileParams));
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
        LinkedHashMap<String, String> stringParams = httpRequestBody.getStringParams();
        if (stringParams == null) {
            stringParams = new LinkedHashMap<>();
        }
        stringParams.put(key, value);
        return this;
    }

    /**
     * 添加请求体参数
     *
     * @param stringParams
     * @returns
     */
    public RequestBuilder addRequestStringParams(LinkedHashMap<String, String> stringParams) {
        httpRequestBody.setStringParams(HttpUtil.appendStringMap(httpRequestBody.getStringParams(), stringParams));
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestBuilder requestBody4JSon(String jsonStr) {
        httpRequestBody.setStringContent(jsonStr);
        httpRequestBody.setMediaType(MediaType.parse(HttpRequestBody.MEDIA_TYPE_JSON));
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public RequestBuilder requestBody4Text(String textStr) {
        httpRequestBody.setStringContent(textStr);
        httpRequestBody.setMediaType(MediaType.parse(HttpRequestBody.MEDIA_TYPE_PLAIN));
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public RequestBuilder requestBody4Xml(String xmlStr) {
        httpRequestBody.setStringContent(xmlStr);
        httpRequestBody.setMediaType(MediaType.parse(HttpRequestBody.MEDIA_TYPE_XML));
        return this;
    }

    /**
     * 设置字节流请求体
     *
     * @param bytes
     * @return
     */
    public RequestBuilder requestBody4Byte(byte[] bytes) {
        httpRequestBody.setBytes(bytes);
        httpRequestBody.setMediaType(MediaType.parse(HttpRequestBody.MEDIA_TYPE_STREAM));
        return this;
    }

}
