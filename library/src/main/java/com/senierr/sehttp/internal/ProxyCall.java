package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.util.MainThreadExecutor;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 代理Call
 *
 * @author zhouchunjie
 * @date 2018/8/19
 */
public class ProxyCall<T> implements Call {

    private SeHttp seHttp;
    private Call realCall;
    private BaseCallback<T> callback;
    private MainThreadExecutor executor;

    private ProxyCall(SeHttp seHttp, Call call, BaseCallback<T> callback) {
        this.seHttp = seHttp;
        this.realCall = call;
        this.callback = callback;
        executor = MainThreadExecutor.getInstance();
    }

    public static <T> ProxyCall<T> newProxyCall(SeHttp seHttp, Call call, BaseCallback<T> callback) {
        return new ProxyCall<>(seHttp, call, callback);
    }

    @Override
    public Request request() {
        return realCall.request();
    }

    @Override
    public Response execute() throws IOException {
        return realCall.execute();
    }

    @Override
    public void enqueue(Callback responseCallback) {
        realCall.enqueue(new Callback() {
            int currentRetryCount = 0;

            @Override
            public void onFailure(Call call, final IOException e) {
                if (!call.isCanceled() && currentRetryCount < seHttp.getRetryCount()) {
                    currentRetryCount++;
                    ProxyCall.this.clone().enqueue(this);
                } else {
                    handleFailure(callback, call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Response responseWrapper = response.newBuilder()
                        .body(new ResponseBodyWrapper(response.body(), callback))
                        .build();
                if (callback != null) {
                    try {
                        T t = callback.convert(responseWrapper);
                        handleSuccess(callback, call, t);
                    } catch (Exception e) {
                        handleFailure(callback, call, e);
                    }
                }
                responseWrapper.close();
            }
        });
    }

    @Override
    public void cancel() {
        realCall.cancel();
    }

    @Override
    public boolean isExecuted() {
        return realCall.isExecuted();
    }

    @Override
    public boolean isCanceled() {
        return realCall.isCanceled();
    }

    @Override
    public ProxyCall<T> clone() {
        return ProxyCall.newProxyCall(seHttp, realCall.clone(), callback);
    }

    /** 执行成功回调 */
    private void handleSuccess(final BaseCallback<T> callback, final Call call, final T t) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (checkIfNeedCallBack(callback, call)) {
                    callback.onSuccess(t);
                }
            }
        });
    }

    /** 执行失败回调 */
    private void handleFailure(final BaseCallback<T> callback, final Call call, final Exception e) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        executor.execute(new Runnable() {
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
