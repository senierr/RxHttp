package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.cache.CacheEntity;
import com.senierr.sehttp.cache.CachePolicy;
import com.senierr.sehttp.callback.BaseCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
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

import static okhttp3.internal.platform.Platform.INFO;

final class CacheRealCall implements Call {

    private final SeHttp seHttp;
    private final Request originalRequest;
    private final boolean forWebSocket;

    private final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    private int currentRetryCount;
    private boolean executed;

    private CacheRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        this.seHttp = seHttp;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(seHttp.getOkHttpClient(), forWebSocket);
    }

    static CacheRealCall newRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        CacheRealCall call = new CacheRealCall(seHttp, originalRequest, forWebSocket);
        call.eventListener = seHttp.getOkHttpClient().eventListenerFactory().create(call);
        return call;
    }

    static CacheRealCall retryRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket, int currentRetryCount) {
        CacheRealCall call = newRealCall(seHttp, originalRequest, forWebSocket);
        call.currentRetryCount = currentRetryCount;
        return call;
    }

    @Override
    public Request request() {
        return originalRequest;
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

    private void captureCallStackTrace() {
        Object callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        retryAndFollowUpInterceptor.setCallStackTrace(callStackTrace);
    }

    @Deprecated
    @Override
    public void enqueue(Callback responseCallback) {
        // replace with enqueueAsync
    }

    public <T> void enqueueAsync(BaseCallback<T> callback, CacheEntity<T> cacheEntity) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        seHttp.getDispatcher().enqueue(new AsyncCall<>(callback, cacheEntity));
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
    public CacheRealCall clone() {
        return CacheRealCall.newRealCall(seHttp, originalRequest, forWebSocket);
    }

    private String toLoggableString() {
        return (isCanceled() ? "canceled " : "")
                + (forWebSocket ? "web socket" : "call")
                + " to " + redactedUrl();
    }

    private String redactedUrl() {
        return originalRequest.url().redact();
    }

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
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());

        return chain.proceed(originalRequest);
    }

    final class AsyncCall<T> extends NamedRunnable {

        private final BaseCallback<T> responseCallback;
        private CacheEntity<T> cacheEntity;

        AsyncCall(BaseCallback<T> responseCallback, CacheEntity<T> cacheEntity) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
            this.cacheEntity = cacheEntity;
        }

        String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        CacheRealCall get() {
            return CacheRealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                if (cacheEntity.getCachePolicy() == CachePolicy.CACHE_ELSE_REQUEST) {
                    T t = getDataFromCache();
                    if (t != null) {
                        sendCacheSuccess(responseCallback, CacheRealCall.this, t);
                        return;
                    }
                }
                if (cacheEntity.getCachePolicy() == CachePolicy.CACHE_THEN_REQUEST
                        && currentRetryCount == 0) {
                    T t = getDataFromCache();
                    if (t != null) {
                        sendCacheSuccess(responseCallback, CacheRealCall.this, t);
                    }
                }

                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    signalledCallback = true;
                    handleFailure(responseCallback, CacheRealCall.this, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
                    handleSuccess(responseCallback, CacheRealCall.this, response);
                }
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                    Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
                } else {
                    eventListener.callFailed(CacheRealCall.this, e);
                    handleFailure(responseCallback, CacheRealCall.this, e);
                }
            } finally {
                seHttp.getDispatcher().finished(this);
            }
        }

        /** 成功处理 */
        private void handleSuccess(BaseCallback<T> callback, final Call call, Response response) {
            final Response responseWrapper = response.newBuilder()
                    .body(new ResponseBodyWrapper(seHttp, response.body(), callback))
                    .build();
            if (callback != null) {
                try {
                    T t = callback.convert(responseWrapper);
                    sendSuccess(callback, call, t);
                    // 存入缓存
                    if (cacheEntity.getCachePolicy() != CachePolicy.NO_CACHE) {
                        putDataToCache(t);
                    }
                } catch (Exception e) {
                    sendFailure(callback, call, e);
                }
            }
            responseWrapper.close();
        }

        /** 失败处理 */
        private void handleFailure(BaseCallback<T> callback, final Call call, final Exception e) {
            if (!isCanceled() && currentRetryCount < seHttp.getRetryCount()) {
                currentRetryCount++;
                retryRealCall(seHttp, originalRequest, false, currentRetryCount)
                        .enqueueAsync(callback, cacheEntity);
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
        private void sendCacheSuccess(final BaseCallback<T> callback,
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
        private void sendSuccess(final BaseCallback<T> callback,
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
        private void sendFailure(final BaseCallback callback, final Call call, final Exception e) {
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
        private boolean checkIfNeedCallBack(final BaseCallback callback, Call call) {
            return callback != null && call != null && !call.isCanceled();
        }
    }
}
