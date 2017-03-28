package com.senierr.sehttp.callback;

import android.os.Handler;

import okhttp3.Response;

/**
 * 请求回调基类
 *
 * Created by zhouchunjie on 2017/3/28.
 */

public abstract class BaseCallback<T> {

    public void onStart() throws Exception {}

    /**
     * 异步，可通过mainScheduler切换至主线程
     *
     * @param response
     * @param mainScheduler
     */
    public abstract void convert(Response response, Handler mainScheduler) throws Exception;

    public abstract void onSuccess(T t) throws Exception;

    public abstract void onError(int responseCode, Exception e);

    public void onAfter() {}
}
