package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求发射器
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
            callback.onBefore(requestBuilder);
        }
        // 构建请求
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
                    handleError(call.isCanceled(), e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Response responseWrapper = response.newBuilder()
                        .body(new ResponseBodyWrapper(response.body(), callback))
                        .build();
                int responseCode = responseWrapper.code();
                if (!responseWrapper.isSuccessful()) {
                    handleError(call.isCanceled(), new Exception("Response is not successful! responseCode: " + responseCode));
                } else {
                    if (callback != null) {
                        try {
                            T t = callback.convert(responseWrapper);
                            handleSuccess(t);
                        } catch (Exception e) {
                            handleError(call.isCanceled(), e);
                        }
                    }
                }
                responseWrapper.close();
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
    private void handleSuccess(final T t) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onSuccess(t);
                    callback.onAfter();
                }
            }
        });
    }

    /**
     * 执行失败回调
     *
     * @param isCanceled
     * @param e
     */
    private void handleError(final boolean isCanceled, final Exception e) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onError(isCanceled, e);
                    callback.onAfter();
                }
            }
        });
    }
}
