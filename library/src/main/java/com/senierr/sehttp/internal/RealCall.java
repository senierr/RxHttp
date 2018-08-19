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

final class RealCall<T> implements Call {

    private final SeHttp seHttp;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    private EventListener eventListener;
    final Request originalRequest;
    final boolean forWebSocket;
    private boolean executed;
    /** 失败重试次数 */
    private int currentRetryCount = 0;

    private RealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        this.seHttp = seHttp;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(seHttp.getOkHttpClient(), forWebSocket);
    }

    static RealCall newRealCall(SeHttp seHttp, Request originalRequest, boolean forWebSocket) {
        RealCall call = new RealCall(seHttp, originalRequest, forWebSocket);
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

    @Override
    public void enqueue(Callback responseCallback) {
        // replace with enqueueAsync
    }

    public void enqueueAsync(BaseCallback<T> callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        seHttp.getDispatcher().enqueue(new AsyncCall(callback));
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
    public RealCall clone() {
        // Clone时，保持失败重连次数
        RealCall realCall = RealCall.newRealCall(seHttp, originalRequest, forWebSocket);
        realCall.setCurrentRetryCount(currentRetryCount);
        return realCall;
    }

    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    public void setCurrentRetryCount(int currentRetryCount) {
        this.currentRetryCount = currentRetryCount;
    }

    /** 异步任务 */
    final class AsyncCall extends NamedRunnable {
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

        RealCall get() {
            return RealCall.this;
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    signalledCallback = true;
                    responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
//                    responseCallback.onResponse(RealCall.this, response);

                    final Response responseWrapper = response.newBuilder()
                            .body(new ResponseBodyWrapper(response.body(), responseCallback))
                            .build();
                    if (responseCallback != null) {
                        try {
                            T t = responseCallback.convert(responseWrapper);
                            handleSuccess(responseCallback, RealCall.this, t);
                        } catch (Exception e) {
                            handleFailure(responseCallback, RealCall.this, e);
                        }
                    }
                    responseWrapper.close();
                }
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                    Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
                } else {
                    eventListener.callFailed(RealCall.this, e);
                    responseCallback.onFailure(RealCall.this, e);
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

    private Response getResponseWithInterceptorChain() throws IOException {
        OkHttpClient client = seHttp.getOkHttpClient();
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
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

    private void a() {
        if (!isCanceled() && currentRetryCount < seHttp.getRetryCount()) {
            currentRetryCount++;
            clone().enqueueAsync();
        } else {
            handleFailure(callback, call, e);
        }
    }

    /** 执行成功回调 */
    private void handleSuccess(final BaseCallback<T> callback, final Call call, final T t) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (checkIfNeedCallBack(callback, call)) {
                    callback.onSuccess(t);
                }
            }
        });
    }

    /** 执行失败回调 */
    private void handleFailure(final BaseCallback callback, final Call call, final Exception e) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        seHttp.getDispatcher().enqueueOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (checkIfNeedCallBack(callback, call)) {
                    callback.onFailure(e);
                }
            }
        });
    }

    /** 检查是否执行回调 */
    private static boolean checkIfNeedCallBack(final BaseCallback callback, Call call) {
        return callback != null && call != null && !call.isCanceled();
    }
}
