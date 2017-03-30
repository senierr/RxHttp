package com.senierr.sehttp.emitter;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.request.RequestBuilder;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
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
        if (callback != null) {
            callback.onBefore();
        }
        getNewCall().enqueue(new Callback() {

            int currentRetryCount = 0;

            @Override
            public void onFailure(Call call, final IOException e) {
                if (e instanceof SocketTimeoutException && currentRetryCount < SeHttp.getInstance().getRetryCount()) {
                    currentRetryCount++;
                    getNewCall().enqueue(this);
                } else {
                    if (!call.isCanceled()) {
                        sendError(e);
                    }
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (callback != null) {
                    try {
                        sendSuccess(callback.convert(response));
                    } catch (Exception e) {
                        sendError(e);
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
        return getNewCall().execute();
    }

    /**
     * 创建Call对象
     *
     * @return
     */
    private Call getNewCall() {
        return SeHttp.getInstance().getOkHttpClient().newCall(requestBuilder.build(callback));
    }

    /**
     * 执行成功回调
     *
     * @param t
     */
    private void sendSuccess(final T t) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    callback.onSuccess(t);
                    callback.onAfter();
                } catch (Exception e) {
                    sendError(e);
                }
            }
        });
    }

    /**
     * 执行错误回调
     *
     * @param e
     */
    private void sendError(final Exception e) {
        if (callback == null) {
            return;
        }
        SeHttp.getInstance().getMainScheduler().post(new Runnable() {
            @Override
            public void run() {
                callback.onError(e);
                callback.onAfter();
            }
        });
    }
}
