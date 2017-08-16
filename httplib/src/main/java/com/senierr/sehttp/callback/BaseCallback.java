package com.senierr.sehttp.callback;

import com.senierr.sehttp.convert.Converter;
import com.senierr.sehttp.request.RequestBuilder;

/**
 * 请求回调基类
 *
 * Created by zhouchunjie on 2017/3/28.
 */

public abstract class BaseCallback<T> implements Converter<T> {

    /**
     * 线程：UI线程
     *
     * 请求开始前回调，可通过requestBuilder修改请求
     *
     * @param requestBuilder 请求构造器
     */
    public void onBefore(RequestBuilder requestBuilder) {}

    /**
     * 线程：UI线程
     *
     * 文件上传进度回调
     *
     * @param currentSize 当前已上传大小
     * @param totalSize 上传文件总大小
     * @param progress 进度0~100
     * @param networkSpeed 网速
     */
    public void uploadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

    /**
     * 线程：UI线程
     *
     * 文件下载进度回调
     *
     * @param currentSize 当前已下载大小
     * @param totalSize 下载文件总大小
     * @param progress 进度0~100
     * @param networkSpeed 网速
     */
    public void downloadProgress(long currentSize, long totalSize, int progress, long networkSpeed) {}

    /**
     * 线程：UI线程
     *
     * 请求成功回调
     *
     * @param t 泛型
     */
    public abstract void onSuccess(T t);

    /**
     * 线程：UI线程
     *
     * 请求异常回调
     *
     * @param e 捕获的异常
     */
    public void onError(Exception e) {}

    /**
     * 线程：UI线程
     *
     * 请求结束回调
     */
    public void onAfter() {}
}
