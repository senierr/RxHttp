package com.senierr.sehttp.emitter;

import android.text.TextUtils;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.CacheEntity;
import com.senierr.sehttp.cache.CacheMode;
import com.senierr.sehttp.cache.disk.DiskLruCacheHelper;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.request.RequestBuilder;
import com.senierr.sehttp.util.EncryptUtil;
import com.senierr.sehttp.util.SeLogger;

import java.io.IOException;

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
        // Before回调
        if (callback != null) {
            SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                @Override
                public void run() {
                    callback.onBefore(requestBuilder);
                }
            });
        }

        // 获取缓存池
        SeHttp.getInstance().getThreadPoolUtils().execute(new Runnable() {
            @Override
            public void run() {
                // 先读取缓存，成功则使用缓存，失败请求网络
                if (requestBuilder.getCacheMode() == CacheMode.CACHE_FAILED_REQUEST) {
                    SeLogger.d("CACHE_FAILED_REQUEST");
                    CacheEntity<T> cacheEntity = readCache();
                    if (cacheEntity != null) {
                        sendSuccess(null, cacheEntity.getCacheContent(), true);
                        return;
                    }
                }
                // 先读取缓存，无论成功与否，然后请求网络
                if (requestBuilder.getCacheMode() == CacheMode.CACHE_THEN_REQUEST) {
                    SeLogger.d("CACHE_THEN_REQUEST");
                    CacheEntity<T>  cacheEntity = readCache();
                    if (cacheEntity != null) {
                        sendSuccess(null, cacheEntity.getCacheContent(), true);
                    }
                }

                final Request request = requestBuilder.build(callback);
                // 网络请求
                getNewCall(request).enqueue(new Callback() {
                    int currentRetryCount = 0;

                    @Override
                    public void onFailure(Call call, final IOException e) {
                        if (!call.isCanceled() && currentRetryCount < SeHttp.getInstance().getRetryCount()) {
                            currentRetryCount++;
                            getNewCall(request).enqueue(this);
                        } else {
                            // 先请求网络，成功则使用网络，失败读取缓存
                            if (requestBuilder.getCacheMode() == CacheMode.REQUEST_FAILED_CACHE) {
                                SeLogger.d("REQUEST_FAILED_CACHE");
                                CacheEntity<T> cacheEntity = readCache();
                                if (cacheEntity != null) {
                                    sendSuccess(null, cacheEntity.getCacheContent(), true);
                                    return;
                                }
                            }
                            sendError(call, e);
                        }
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        if (callback != null) {
                            try {
                                T t = callback.convert(response);
                                sendSuccess(call, t, false);
                                // 缓存
                                if (requestBuilder.getCacheMode() != CacheMode.NO_CACHE) {
                                    writeCache(t);
                                }
                            } catch (Exception e) {
                                sendError(call, e);
                            }
                        }
                        response.close();
                    }
                });
            }
        });
    }

    /**
     * 同步执行
     *
     * @return
     */
    public Response execute() throws IOException {
        return getNewCall(requestBuilder.build(callback)).execute();
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
                    callback.onSuccess(t, isCache);
                    callback.onAfter();
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
    private CacheEntity<T> readCache() {
        DiskLruCacheHelper diskLruCacheHelper = SeHttp.getInstance().getDiskLruCacheHelper();
        String cacheKey = EncryptUtil.encryptMD5ToString(requestBuilder.getCacheKey());
        if (!TextUtils.isEmpty(cacheKey) && diskLruCacheHelper != null) {
            CacheEntity<T> cacheEntity = diskLruCacheHelper.getAsSerializable(cacheKey);
            if (cacheEntity != null) {
                // 检查是否超时
                if (System.currentTimeMillis() - cacheEntity.getUpdateDate() <= requestBuilder.getCacheTime()) {
                    return cacheEntity;
                } else {
                    diskLruCacheHelper.remove(cacheKey);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 写入缓存
     *
     * @param cacheObject
     */
    private void writeCache(T cacheObject) {
        DiskLruCacheHelper diskLruCacheHelper = SeHttp.getInstance().getDiskLruCacheHelper();
        if (cacheObject == null || diskLruCacheHelper == null) {
            return;
        }
        CacheEntity<T> cacheEntity = new CacheEntity<>();
        cacheEntity.setKey(requestBuilder.getCacheKey());
        cacheEntity.setCacheContent(cacheObject);
        cacheEntity.setUpdateDate(System.currentTimeMillis());
        diskLruCacheHelper.put(EncryptUtil.encryptMD5ToString(cacheEntity.getKey()), cacheEntity);
    }
}
