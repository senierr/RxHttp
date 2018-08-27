package com.senierr.sehttp.internal;

import android.text.TextUtils;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.CacheEntity;
import com.senierr.sehttp.cache.CachePolicy;
import com.senierr.sehttp.callback.Callback;
import com.senierr.sehttp.util.Utils;

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
public final class RequestFactory {

    private SeHttp seHttp;
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
    // 请求体构造器
    private RequestBodyBuilder requestBodyBuilder;
    // 缓存协议
    private String cacheKey;
    // 缓存协议
    private CachePolicy cachePolicy = CachePolicy.NO_CACHE;
    // 缓存有效时长
    private long cacheDuration;

    public RequestFactory(SeHttp seHttp, String method, String url) {
        this.seHttp = seHttp;
        this.method = method;
        this.url = url;
        requestBodyBuilder = new RequestBodyBuilder();
    }

    /**
     * 创建请求
     *
     * @return
     */
    public Request create(Callback callback) {
        // 封装RequestBody
        RequestBody requestBody = requestBodyBuilder.build();
        if (requestBody != null) {
            requestBody = new RequestBodyWrapper(seHttp, requestBody, callback);
        }
        // 生成Request
        Request.Builder requestBuilder = new Request.Builder();
        httpUrlParams = Utils.mergeMap(seHttp.getCommonUrlParams(), httpUrlParams);
        httpHeaders = Utils.mergeMap(seHttp.getCommonHeaders(), httpHeaders);
        if (httpUrlParams != null && !httpUrlParams.isEmpty()) {
            url = Utils.buildUrlParams(url, httpUrlParams);
        }
        if (httpHeaders != null && !httpHeaders.isEmpty()) {
            requestBuilder.headers(Utils.buildHeaders(httpHeaders));
        }
        if (tag != null) {
            requestBuilder.tag(tag);
        }
        requestBuilder.method(method, requestBody);
        requestBuilder.url(url);
        return requestBuilder.build();
    }

    /**
     * 执行异步请求
     *
     * @param callback
     */
    public <T> void execute(Callback<T> callback) {
        // 检查缓存参数
        if (cachePolicy != CachePolicy.NO_CACHE) {
            if (TextUtils.isEmpty(cacheKey)) {
                throw new IllegalArgumentException("CacheKey must be not null!");
            }
            if (cacheDuration <= 0) {
                throw new IllegalArgumentException("CacheDuration must be greater than 0!");
            }

        }
        CacheEntity<T> cacheEntity = new CacheEntity<>();
        cacheEntity.setCacheKey(cacheKey);
        cacheEntity.setCachePolicy(cachePolicy);
        cacheEntity.setCacheDuration(cacheDuration);

        CacheCall.newCacheCall(seHttp, create(callback), cacheEntity).enqueue(callback);
    }

    /**
     * 执行同步请求
     *
     * @return
     * @throws IOException
     */
    public Response execute() throws IOException {
        return CacheCall.newCacheCall(seHttp, create(null), null).execute();
    }

    /**
     * 设置缓存Key
     *
     * @param cacheKey
     * @return
     */
    public RequestFactory cacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    /**
     * 设置缓存协议
     *
     * @param cachePolicy
     * @return
     */
    public RequestFactory cachePolicy(CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
        return this;
    }

    /**
     * 设置缓存时长
     *
     * @param cacheDuration
     * @return
     */
    public RequestFactory cacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
        return this;
    }

    /**
     * 设置标签
     *
     * @param tag
     * @return
     */
    public RequestFactory tag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 添加请求参数
     *
     * @param key
     * @param value
     * @return
     */
    public RequestFactory addUrlParam(String key, String value) {
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
    public RequestFactory addUrlParams(LinkedHashMap<String, String> params) {
        httpUrlParams = Utils.mergeMap(httpUrlParams, params);
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
    public RequestFactory addHeaders(LinkedHashMap<String, String> headers) {
        httpHeaders = Utils.mergeMap(httpHeaders, headers);
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
        LinkedHashMap<String, File> fileParams = requestBodyBuilder.getFileParams();
        if (fileParams == null) {
            fileParams = new LinkedHashMap<>();
        }
        fileParams.put(key, file);
        requestBodyBuilder.setFileParams(fileParams);
        return this;
    }

    /**
     * 添加多个文件参数
     *
     * @param fileParams
     * @returns
     */
    public RequestFactory addRequestFileParams(LinkedHashMap<String, File> fileParams) {
        requestBodyBuilder.setFileParams(Utils.mergeMap(requestBodyBuilder.getFileParams(), fileParams));
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
        LinkedHashMap<String, String> stringParams = requestBodyBuilder.getStringParams();
        if (stringParams == null) {
            stringParams = new LinkedHashMap<>();
        }
        stringParams.put(key, value);
        requestBodyBuilder.setStringParams(stringParams);
        return this;
    }

    /**
     * 添加多个字符串参数
     *
     * @param stringParams
     * @returns
     */
    public RequestFactory addRequestStringParams(LinkedHashMap<String, String> stringParams) {
        requestBodyBuilder.setStringParams(Utils.mergeMap(requestBodyBuilder.getStringParams(), stringParams));
        return this;
    }

    /**
     * 设置JSON格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestFactory setRequestBody4JSon(String jsonStr) {
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
    public RequestFactory setRequestBody4Text(String textStr) {
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
    public RequestFactory setRequestBody4Xml(String xmlStr) {
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
    public RequestFactory setRequestBody4Byte(byte[] bytes) {
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
    public RequestFactory setRequestBody(RequestBody requestBody) {
        requestBodyBuilder.setRequestBody(requestBody);
        return this;
    }

    public RequestFactory setRequestBody(MediaType contentType, File file) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, file));
        return this;
    }

    public RequestFactory setRequestBody(MediaType contentType, byte[] content, int offset, int byteCount) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content, offset, byteCount));
        return this;
    }

    public RequestFactory setRequestBody(MediaType contentType, byte[] content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestFactory setRequestBody(MediaType contentType, ByteString content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }

    public RequestFactory setRequestBody(MediaType contentType, String content) {
        requestBodyBuilder.setRequestBody(RequestBody.create(contentType, content));
        return this;
    }
}