package com.senierr.sehttp.internal;

import android.util.Log;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.OnDownloadListener;
import com.senierr.sehttp.callback.OnUploadListener;
import com.senierr.sehttp.converter.Converter;
import com.senierr.sehttp.entity.FileMap;
import com.senierr.sehttp.entity.HttpHeaders;
import com.senierr.sehttp.entity.HttpUrlParams;
import com.senierr.sehttp.util.HttpUtil;
import com.senierr.sehttp.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import okhttp3.Call;
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

    private SeHttp seHttp;
    // 请求方法
    private String method;
    // 请求
    private String url;
    // url参数
    private LinkedHashMap<String, String> httpUrlParams;
    // 请求头
    private LinkedHashMap<String, String> httpHeaders;
    // 请求体
    private RequestBodyBuilder requestBodyBuilder;
    // 上传进度
    private OnUploadListener onUploadListener;
    // 下载进度
    private OnDownloadListener onDownloadListener;

    public RequestBuilder(SeHttp seHttp, String method, String url) {
        this.seHttp = seHttp;
        this.method = method;
        this.url = url;
        requestBodyBuilder = new RequestBodyBuilder();
    }

    /**
     * 执行同步请求
     *
     * @return
     * @throws IOException
     */
    public <T> T executeWith(Converter<T> converter) throws IOException {
        // 封装RequestBody
        RequestBody requestBody = requestBodyBuilder.build();
        if (requestBody != null) {
            requestBody = new RequestBodyWrapper(requestBody, onUploadListener);
        }
        // 生成Request
        Request.Builder requestBuilder = new Request.Builder();
        httpUrlParams = HttpUtil.appendStringMap(httpUrlParams, seHttp.getBuilder().getCommonUrlParams());
        httpHeaders = HttpUtil.appendStringMap(httpHeaders, seHttp.getBuilder().getCommonHeaders());
        if (httpUrlParams != null && !httpUrlParams.isEmpty()) {
            url = HttpUrlParams.buildParams(url, httpUrlParams);
        }
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            requestBuilder.headers(HttpHeaders.buildHeaders(httpHeaders));
        }
        requestBuilder.method(method, requestBody);
        requestBuilder.url(url);
        Request request = requestBuilder.build();
        // 生成Call
        Call call = seHttp.getBuilder()
                .getOkHttpClientBuilder()
                .build()
                .newCall(request);
        // 请求
        Response response = call.execute();
        // 封装ResponseBody
        Response newResponse = response.newBuilder()
                .body(new ResponseBodyWrapper(response.body(), onDownloadListener))
                .build();
        // 转化
        try {
            return converter.onConvert(newResponse);
        } catch (Exception e) {
            LogUtil.logW(Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * 添加请求参数
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
     * 添加头部
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
     * 添加文件参数
     *
     * @param key
     * @param file
     * @return
     */
    public RequestBuilder addRequestParam(String key, File file) {
        FileMap fileParams = requestBodyBuilder.getFileParams();
        if (fileParams == null) {
            fileParams = new FileMap();
        }
        fileParams.add(key, file);
        requestBodyBuilder.setFileParams(fileParams);
        return this;
    }

    /**
     * 添加字符串参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder addRequestParam(String key, String value) {
        LinkedHashMap<String, String> stringParams = requestBodyBuilder.getStringParams();
        if (stringParams == null) {
            stringParams = new LinkedHashMap<>();
        }
        stringParams.put(key, value);
        requestBodyBuilder.setStringParams(stringParams);
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestBuilder setRequestBody4JSon(String jsonStr) {
        requestBodyBuilder.setStringContent(jsonStr);
        requestBodyBuilder.setMediaType(MediaType.parse(RequestBodyBuilder.MEDIA_TYPE_JSON));
        return this;
    }

    /**
     * 设置文本格式请求体
     *
     * @param textStr
     * @returne
     */
    public RequestBuilder setRequestBody4Text(String textStr) {
        requestBodyBuilder.setStringContent(textStr);
        requestBodyBuilder.setMediaType(MediaType.parse(RequestBodyBuilder.MEDIA_TYPE_PLAIN));
        return this;
    }

    /**
     * 设置XML格式请求体
     *
     * @param xmlStr
     * @returne
     */
    public RequestBuilder setRequestBody4Xml(String xmlStr) {
        requestBodyBuilder.setStringContent(xmlStr);
        requestBodyBuilder.setMediaType(MediaType.parse(RequestBodyBuilder.MEDIA_TYPE_XML));
        return this;
    }

    /**
     * 设置字节流请求体
     *
     * @param bytes
     * @return
     */
    public RequestBuilder setRequestBody4Byte(byte[] bytes) {
        requestBodyBuilder.setBytes(bytes);
        requestBodyBuilder.setMediaType(MediaType.parse(RequestBodyBuilder.MEDIA_TYPE_STREAM));
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody
     * @return
     */
    public RequestBuilder setRequestBody(RequestBody requestBody) {
        requestBodyBuilder.setRequestBody(requestBody);
        return this;
    }

    public RequestBuilder setRequestBody(MediaType contentType, File file) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, file));
        return this;
    }

    public RequestBuilder setRequestBody(MediaType contentType, byte[] content, int offset, int byteCount) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content, offset, byteCount));
        return this;
    }

    public RequestBuilder setRequestBody(MediaType contentType, byte[] content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestBuilder setRequestBody(MediaType contentType, ByteString content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestBuilder setRequestBody(MediaType contentType, String content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    /**
     * 设置上传进度
     *
     * @param onUploadListener
     * @return
     */
    public RequestBuilder setOnUploadListener(OnUploadListener onUploadListener) {
        this.onUploadListener = onUploadListener;
        return this;
    }

    /**
     * 设置下载进度
     *
     * @param onDownloadListener
     * @return
     */
    public RequestBuilder setOnDownloadListener(OnDownloadListener onDownloadListener) {
        this.onDownloadListener = onDownloadListener;
        return this;
    }
}
