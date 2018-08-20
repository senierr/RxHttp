package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
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

final class ProxyRealCall implements Call {

    private final SeHttp seHttp;
    private final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    private final Request originalRequest;
    private final boolean forWebSocket;
    private boolean executed;
    /** 失败重试次数 */
    private int currentRetryCount = 0;

    private ProxyRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        this.seHttp = seHttp;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(seHttp.getOkHttpClient(), forWebSocket);
    }

    static ProxyRealCall newRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        ProxyRealCall call = new ProxyRealCall(seHttp, originalRequest, forWebSocket);
        call.eventListener = seHttp.getOkHttpClient().eventListenerFactory().create(call);
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

    /** 异步任务入栈 */
    public <T> void enqueueAsync(BaseCallback<T> callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        seHttp.getDispatcher().enqueue(new AsyncCall<>(callback));
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
    public ProxyRealCall clone() {
        // Clone时，保持失败重连次数
        ProxyRealCall realCall = ProxyRealCall.newRealCall(seHttp, originalRequest, forWebSocket);
        realCall.setCurrentRetryCount(currentRetryCount);
        return realCall;
    }

    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    public void setCurrentRetryCount(int currentRetryCount) {
        this.currentRetryCount = currentRetryCount;
    }

    final class AsyncCall<T> extends NamedRunnable {
        private final BaseCallback<T> responseCallback;

        AsyncCall(BaseCallback<T> responseCallback) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        ProxyRealCall get() {
            return ProxyRealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    signalledCallback = true;
                    handleFailure(responseCallback, ProxyRealCall.this, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
                    handleSuccess(responseCallback, ProxyRealCall.this, response);
                }
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                    Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
                } else {
                    eventListener.callFailed(ProxyRealCall.this, e);
                    handleFailure(responseCallback, ProxyRealCall.this, e);
                }
            } finally {
                seHttp.getDispatcher().finished(this);
            }
        }
    }

    /**
     * Returns a string that describes this call. Doesn't include a full URL as that might contain
     * sensitive information.
     */
    private String toLoggableString() {
        return (isCanceled() ? "canceled " : "")
                + (forWebSocket ? "web socket" : "call")
                + " to " + redactedUrl();
    }

    private String redactedUrl() {
        return originalRequest.url().redact();
    }

    /**
     * 处理应用拦截器和网络拦截器，并返回处理结果
     */
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

    /** 成功处理 */
    private <T> void handleSuccess(BaseCallback<T> callback, final Call call, Response response) {
        final Response responseWrapper = response.newBuilder()
                .body(new ResponseBodyWrapper(seHttp, response.body(), callback))
                .build();
        if (callback != null) {
            try {
                T t = callback.convert(responseWrapper);
                sendSuccess(callback, call, t);
            } catch (Exception e) {
                sendFailure(callback, call, e);
            }
        }
        responseWrapper.close();
    }

    /** 失败处理 */
    private <T> void handleFailure(BaseCallback<T> callback, final Call call, final Exception e) {
        if (!isCanceled() && currentRetryCount < seHttp.getRetryCount()) {
            currentRetryCount++;
            ProxyRealCall.this.clone().enqueueAsync(callback);
        } else {
            sendFailure(callback, call, e);
        }
    }

    /** 执行成功回调 */
    private <T> void sendSuccess(final BaseCallback<T> callback, final Call call, final T t) {
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

    /** 执行失败回调 */
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

    /** 检查是否执行回调 */
    private static boolean checkIfNeedCallBack(final BaseCallback callback, Call call) {
        return callback != null && call != null && !call.isCanceled();
    }
}
