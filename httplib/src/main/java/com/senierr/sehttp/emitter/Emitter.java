package com.senierr.sehttp.emitter;

import android.text.TextUtils;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.Cache;
import com.senierr.sehttp.cache.CacheEntity;
import com.senierr.sehttp.cache.CacheMode;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.EncryptUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求发射器
 *
 * 用于发送执行，取消请求，缓存及事件分发等
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class Emitter<T> {

    // 请求实体
    private RequestBuilder requestBuilder;
    // 回调
    private BaseCallback<T> callback;

    public Emitter(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    /**
     * 异步执行
     *
     * @param baseCallback
     */
    public void execute(BaseCallback<T> baseCallback) {
        this.callback = baseCallback;
        final Request request = requestBuilder.build(callback);

        if (callback != null) {
            callback.onBefore();
        }
        getNewCall(request).enqueue(new Callback() {
            int currentRetryCount = 0;

            @Override
            public void onFailure(Call call, final IOException e) {
                if (!call.isCanceled() && currentRetryCount < SeHttp.getInstance().getRetryCount()) {
                    currentRetryCount++;
                    getNewCall(request).enqueue(this);
                } else {
                    sendError(call, e);
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (callback != null) {
                    try {
                        sendSuccess(call, callback.convert(response));
                    } catch (Exception e) {
                        sendError(call, e);
                    }
                }
                response.close();
            }
        });
    }

    /**
     * 同步执行
     *
     * @return
     */
    public Response execute() throws IOException {
        Request request = requestBuilder.build(callback);
        return getNewCall(request).execute();
    }

    /**
     * 创建Call对象
     *
     * @return
     */
    private Call getNewCall(Request request) {
        return SeHttp.getInstance().getOkHttpClient().newCall(request);
    }

    /**
     * 执行成功回调
     *
     * @param t
     */
    private void sendSuccess(final Call call, final T t) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(t);
                    callback.onAfter();
                    // 判断是否缓存
                    if (requestBuilder.getCacheMode() != CacheMode.NO_CACHE
                            && t.getClass() == String.class
                            && !TextUtils.isEmpty(requestBuilder.getCacheKey())) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CacheEntity cacheEntity = new CacheEntity();
                                cacheEntity.setKey(requestBuilder.getCacheKey());
                                cacheEntity.setCacheContent((String) t);
                                cacheEntity.setUpdateDate(System.currentTimeMillis());
                                Cache.writeCache(cacheEntity);
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    sendError(call, e);
                }
            }
        });
    }

    /**
     * 执行失败回调
     *
     * @param e
     */
    private void sendError(Call call, final Exception e) {
        if (callback == null) {
            return;
        }

        // 检查缓存
        String cacheKey = requestBuilder.getCacheKey();
        if (!TextUtils.isEmpty(cacheKey)) {



        }

        if (!call.isCanceled()) {
            SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onError(e);
                    callback.onAfter();
                }
            });
        }
    }
}
