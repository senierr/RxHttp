package com.senierr.sehttp.emitter;

import android.text.TextUtils;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.Cache;
import com.senierr.sehttp.cache.CacheEntity;
import com.senierr.sehttp.cache.CacheMode;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.EncryptUtils;
import com.senierr.sehttp.util.SeLogger;

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

        // 缓存处理
        if (requestBuilder.getCacheMode() == null) {
            requestBuilder.cacheMode(CacheMode.NO_CACHE);
        }
        if (requestBuilder.getCacheMode() == CacheMode.CACHE_FAILED_REQUEST) {
            // 先读取缓存，成功则使用缓存，失败请求网络
            CacheEntity cacheEntity = readCache();
            if (cacheEntity != null) {
                sendSuccess(null, (T) cacheEntity.getCacheContent(), true);
                return;
            }
        }
        if (requestBuilder.getCacheMode() == CacheMode.CACHE_THEN_REQUEST) {
            // 先读取缓存，无论成功与否，然后请求网络
            CacheEntity cacheEntity = readCache();
            if (cacheEntity != null) {
                sendSuccess(null, (T) cacheEntity.getCacheContent(), true);
            }
        }

        // 网络请求
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
                        sendSuccess(call, callback.convert(response), false);
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
    private void sendSuccess(final Call call, final T t, final boolean isCache) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(t, false);
                    callback.onAfter();
                    // 判断是否缓存
                    if (requestBuilder.getCacheMode() != CacheMode.NO_CACHE
                            && t.getClass() == String.class
                            && !isCache
                            && ((String) t).length() <= SeHttp.getInstance().getCacheConfig().getMaxSize()
                            && !TextUtils.isEmpty(requestBuilder.getCacheKey())) {
                        writeCache((String) t);
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
    private void sendError(final Call call, final Exception e) {
        if (callback == null) {
            return;
        }

        if (call == null || !call.isCanceled()) {
            SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                @Override
                public void run() {
                    if (requestBuilder.getCacheMode() == CacheMode.REQUEST_FAILED_CACHE) {

                    }
                    // 检查缓存
                    String cacheKey = requestBuilder.getCacheKey();
                    if (!TextUtils.isEmpty(cacheKey)) {
                        CacheEntity cacheEntity = Cache.readCache(cacheKey);
                        if (cacheEntity != null) {
                            sendSuccess(call, (T) cacheEntity.getCacheContent());
                            return;
                        }
                    }
                    callback.onError(e);
                    callback.onAfter();
                }
            });
        }
    }

    /**
     * 获取缓存
     *
     * @return
     */
    private CacheEntity readCache() {
        String cacheKey = requestBuilder.getCacheKey();
        if (!TextUtils.isEmpty(cacheKey)) {
            CacheEntity cacheEntity = Cache.readCache(cacheKey);
            if (cacheEntity != null
                    && System.currentTimeMillis() - cacheEntity.getUpdateDate()
                    <= (requestBuilder.getCacheTime() <= 0
                    ? 1000 * 3600 * 24 : requestBuilder.getCacheTime())) {
                return cacheEntity;
            }
        }
        return null;
    }

    /**
     * 写入缓存
     *
     * @param responseStr
     */
    private void writeCache(final String responseStr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CacheEntity cacheEntity = new CacheEntity();
                cacheEntity.setKey(requestBuilder.getCacheKey());
                cacheEntity.setCacheContent(responseStr);
                cacheEntity.setUpdateDate(System.currentTimeMillis());
                Cache.writeCache(cacheEntity);
            }
        }).start();
    }
}
