package com.senierr.sehttp.emitter;

import android.os.Handler;
import android.os.Looper;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.BaseCallback;
import com.senierr.sehttp.request.RequestBuilder;

import java.io.IOException;

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
    // OkHttp发射器
    private Call call;
    // 回调
    private BaseCallback<T> callback;

    public Emitter(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
        this.call = SeHttp.getInstance().getOkHttpClient().newCall(requestBuilder.build());
    }

    /**
     * 异步执行
     *
     * @param baseCallback
     */
    public void execute(BaseCallback<T> baseCallback) {
        this.callback = baseCallback;
        if (callback != null) {
            callback.onStart();
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                // TODO: 2017/3/28 处理失败重连

                if (!call.isCanceled()) {
                    sendError(e);
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
        return call.execute();
    }


    /**
     * 执行成功回调
     *
     * @param t
     */
    public void sendSuccess(final T t) {
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
    public void sendError(final Exception e) {
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
