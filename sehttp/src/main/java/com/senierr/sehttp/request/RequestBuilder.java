package com.senierr.sehttp.request;

import android.util.Log;

import com.senierr.sehttp.util.HttpUtil;
import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
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
    private Map<String, String> urlParams;
    // 请求体
    private RequestBody requestBody;
    // 请求头
    private Map<String, String> headers;

    public RequestBuilder(String method, String url) {
        this.method = method;
        this.url = url;
    }

    /**
     * 创建请求
     *
     * @return
     */
    private Request build() {
        Request.Builder builder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            builder.headers(HttpUtil.buildHeaders(headers));
        }
        if (tag != null) {
            builder.tag(tag);
        }
        if (urlParams != null && !urlParams.isEmpty()) {
            url += HttpUtil.buildUrlParams(urlParams);
        }
        builder.method(method, requestBody);
        builder.url(url);
        return builder.build();
    }

    /**
     * 执行请求
     *
     * @param callback
     */
    public void execute(final BaseCallback callback) {
        if (callback != null) {
            try {
                callback.onStart();
            } catch (Exception e) {
                callback.onError(-1, e);
                callback.onAfter();
                return;
            }
        }
        Call call = SeHttp.getInstance().getOkHttpClient().newCall(build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                // TODO: 2017/3/28 处理失败重连

                if (!call.isCanceled() && callback != null) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(-1, e);
                            callback.onAfter();
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (callback != null) {
                    try {
                        callback.convert(response, SeHttp.getInstance().getMainScheduler());
                    } catch (final Exception e) {
                        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(-1, e);
                                callback.onAfter();
                            }
                        });
                    }
                }
            }
        });
    }

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
        if (urlParams == null) {
            urlParams = new HashMap<>();
        }
        urlParams.put(key, value);
        return this;
    }

    /**
     * 添加多个请求参数
     *
     * @param urlParams
     * @return
     */
    public RequestBuilder urlParams(Map<String, String> urlParams) {
        this.urlParams = urlParams;
        return this;
    }

    /**
     * 添加单个头部
     *
     * @param key
     * @param value
     * @return
     */
    public RequestBuilder addHead(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    /**
     * 添加多个头部
     *
     * @param headers
     * @return
     */
    public RequestBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * 设置JSon格式请求体
     *
     * @param jsonStr
     * @return
     */
    public RequestBuilder jsonRequestBody(String jsonStr) {
        this.requestBody = HttpUtil.buildRequestBody(jsonStr);
        return this;
    }

    /**
     * 设置键值对请求体
     *
     * @param bodyParams
     * @return
     */
    public RequestBuilder requestBody(Map<String, String> bodyParams) {
        this.requestBody = HttpUtil.buildRequestBody(bodyParams);
        return this;
    }
}
