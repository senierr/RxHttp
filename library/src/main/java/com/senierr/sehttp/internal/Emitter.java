package com.senierr.sehttp.internal;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求发射器
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class Emitter<T> {

    private SeHttp seHttp;
    private Request request;

    public Emitter(SeHttp seHttp, Request request) {
        this.seHttp = seHttp;
        this.request = request;
    }

    /**
     * 同步请求
     *
     * @return
     */
    public Response execute() throws IOException {
        return getNewCall().execute();
    }

    /**
     * 异步请求
     */
    public void execute(final BaseCallback<T> callback) {
        getNewCall().enqueue(new okhttp3.Callback() {
            int currentRetryCount = 0;

            @Override
            public void onFailure(Call call, final IOException e) {
                if (!call.isCanceled() && currentRetryCount < seHttp.getBuilder().getRetryCount()) {
                    currentRetryCount++;
                    getNewCall().enqueue(this);
                } else {
                    handleFailure(callback, call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final Response responseWrapper = response.newBuilder()
                        .body(new ResponseBodyWrapper(seHttp, response.body(), callback))
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

    /**
     * 创建Call对象
     *
     * @return
     */
    private Call getNewCall() {
        return seHttp.getBuilder().getOkHttpClient().newCall(request);
    }

    /**
     * 执行成功回调
     *
     * @param t
     */
    private void handleSuccess(final BaseCallback<T> callback, final Call call, final T t) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        seHttp.getBuilder().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                if (checkIfNeedCallBack(callback, call)) {
                    callback.onSuccess(t);
                }
            }
        });
    }

    /**
     * 执行失败回调
     *
     * @param call
     * @param e
     */
    private void handleFailure(final BaseCallback<T> callback, final Call call, final Exception e) {
        if (!checkIfNeedCallBack(callback, call)) {
            return;
        }
        seHttp.getBuilder().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                if (checkIfNeedCallBack(callback, call)) {
                    callback.onFailure(e);
                }
            }
        });
    }

    /**
     * 检查是否执行回调
     *
     * @param callback
     * @param call
     * @return
     */
    private static boolean checkIfNeedCallBack(final BaseCallback callback, Call call) {
        return callback != null && call != null && !call.isCanceled();
    }
}
