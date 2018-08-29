package com.senierr.http.internal;

import com.senierr.http.RxHttp;
import com.senierr.http.cache.CacheEntity;
import com.senierr.http.cache.CachePolicy;
import com.senierr.http.callback.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.NamedRunnable;
import okhttp3.internal.connection.ConnectInterceptor;
import okhttp3.internal.http.BridgeInterceptor;
import okhttp3.internal.http.CallServerInterceptor;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import okhttp3.internal.platform.Platform;

final class CacheCall<T> implements Call<T> {

    private final RxHttp seHttp;
    private final Request originalRequest;
    private final CacheEntity<T> cacheEntity;

    private final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    private int currentRetryCount;
    private boolean executed;

    private CacheCall(RxHttp seHttp, Request originalRequest, CacheEntity<T> cacheEntity) {
        this.seHttp = seHttp;
        this.originalRequest = originalRequest;
        this.cacheEntity = cacheEntity;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(seHttp.getOkHttpClient(), false);
    }

    static <T> CacheCall<T> newCacheCall(RxHttp seHttp, Request originalRequest, CacheEntity<T> cacheEntity) {
        CacheCall<T> call = new CacheCall<>(seHttp, originalRequest, cacheEntity);
        call.eventListener = seHttp.getOkHttpClient().eventListenerFactory().create(call);
        return call;
    }

    static <T> CacheCall<T> retryCacheCall(RxHttp seHttp, Request originalRequest, CacheEntity<T> cacheEntity, int currentRetryCount) {
        CacheCall<T> call = newCacheCall(seHttp, originalRequest, cacheEntity);
        call.currentRetryCount = currentRetryCount;
        return call;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public void cancel() {
        retryAndFollowUpInterceptor.cancel();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return retryAndFollowUpInterceptor.isCanceled();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public CacheCall clone() {
        return CacheCall.newCacheCall(seHttp, originalRequest, cacheEntity);
    }

    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        try {
            seHttp.getDispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } catch (IOException e) {
            eventListener.callFailed(this, e);
            throw e;
        } finally {
            seHttp.getDispatcher().finished(this);
        }
    }

    @Deprecated
    @Override
    public void enqueue(okhttp3.Callback responseCallback) {
        // 此方法已被enqueue(Callback<T> callback)替换
    }

    @Override
    public void enqueue(Callback<T> callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        seHttp.getDispatcher().enqueue(new AsyncCall(callback));
    }

    private void captureCallStackTrace() {
        Object callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        retryAndFollowUpInterceptor.setCallStackTrace(callStackTrace);
    }

    /** 拦截器处理 */
    private Response getResponseWithInterceptorChain() throws IOException {
        OkHttpClient client = seHttp.getOkHttpClient();
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        /* 取消内部缓存处理 */
//    interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        interceptors.addAll(client.networkInterceptors());
        interceptors.add(new CallServerInterceptor(false));

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());
        return chain.proceed(originalRequest);
    }

    final class AsyncCall extends NamedRunnable {

        private final Callback<T> responseCallback;

        AsyncCall(Callback<T> responseCallback) {
            super("OkHttp %s", originalRequest.url().redact());
            this.responseCallback = responseCallback;
        }

        String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        CacheCall get() {
            return CacheCall.this;
        }

        @Override
        protected void execute() {
            try {
                if (cacheEntity.getCachePolicy() == CachePolicy.CACHE_ELSE_REQUEST) {
                    T t = getDataFromCache();
                    if (t != null) {
                        sendCacheSuccess(responseCallback, CacheCall.this, t);
                        return;
                    }
                }
                if (cacheEntity.getCachePolicy() == CachePolicy.CACHE_THEN_REQUEST
                        && currentRetryCount == 0) {
                    T t = getDataFromCache();
                    if (t != null) {
                        sendCacheSuccess(responseCallback, CacheCall.this, t);
                    }
                }

                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    handleFailure(responseCallback, CacheCall.this, new IOException("Canceled"));
                } else {
                    handleSuccess(responseCallback, CacheCall.this, response);
                }
            } catch (IOException e) {
                eventListener.callFailed(CacheCall.this, e);
                handleFailure(responseCallback, CacheCall.this, e);
            } finally {
                seHttp.getDispatcher().finished(this);
            }
        }

        /** 成功处理 */
        private void handleSuccess(Callback<T> callback, final Call call, Response response) {
            final Response responseWrapper = response.newBuilder()
                    .body(new ProgressResponseBody(seHttp, response.body(), callback))
                    .build();
            if (callback != null) {
                try {
                    T t = callback.converter().convertResponse(responseWrapper);
                    sendSuccess(callback, call, t);
                    // 存入缓存
                    if (cacheEntity.getCachePolicy() != CachePolicy.NO_CACHE) {
                        putDataToCache(t);
                    }
                } catch (Throwable e) {
                    sendFailure(callback, call, e);
                }
            }
            responseWrapper.close();
        }

        /** 失败处理 */
        private void handleFailure(Callback<T> callback, final Call call, final Throwable e) {
            if (!isCanceled() && currentRetryCount < seHttp.getRetryCount()) {
                currentRetryCount++;
                retryCacheCall(seHttp, originalRequest, cacheEntity, currentRetryCount)
                        .enqueue(callback);
            } else {
                // 是否需要返回缓存数据
                if (cacheEntity.getCachePolicy() == CachePolicy.REQUEST_ELSE_CACHE) {
                    T t = getDataFromCache();
                    if (t != null) {
                        sendCacheSuccess(callback, call, t);
                        return;
                    }
                }
                sendFailure(callback, call, e);
            }
        }

        /** 获取缓存实例 */
        private T getDataFromCache() {
            CacheEntity<T> entity = seHttp.getCacheStore().get(cacheEntity.getCacheKey());
            // 验证过期时间
            if (entity != null && entity.getCacheTime() + entity.getCacheDuration() >= System.currentTimeMillis()) {
                return entity.getContent();
            }
            return null;
        }

        /** 缓存实例 */
        private void putDataToCache(T t) {
            cacheEntity.setContent(t);
            cacheEntity.setCacheTime(System.currentTimeMillis());
            seHttp.getCacheStore().put(cacheEntity.getCacheKey(), cacheEntity);
        }

        /** 发送缓存回调 */
        private void sendCacheSuccess(final Callback<T> callback,
                                     final Call call,
                                     final T t) {
            if (checkIfNeedCallBack(callback, call)) {
                seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfNeedCallBack(callback, call)) {
                            callback.onCacheSuccess(t);
                        }
                    }
                });
            }
        }

        /** 发送成功回调 */
        private void sendSuccess(final Callback<T> callback,
                                     final Call call,
                                     final T t) {
            if (checkIfNeedCallBack(callback, call)) {
                seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfNeedCallBack(callback, call)) {
                            callback.onSuccess(t);
                        }
                    }
                });
            }
        }

        /** 发送失败回调 */
        private void sendFailure(final Callback callback, final Call call, final Throwable e) {
            if (checkIfNeedCallBack(callback, call)) {
                seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if (checkIfNeedCallBack(callback, call)) {
                            callback.onFailure(e);
                        }
                    }
                });
            }
        }

        /** 检查是否发送回调 */
        private boolean checkIfNeedCallBack(final Callback callback, Call call) {
            return callback != null && call != null && !call.isCanceled();
        }
    }
}
