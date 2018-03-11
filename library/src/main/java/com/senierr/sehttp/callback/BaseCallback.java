package com.senierr.sehttp.callback;

import com.senierr.sehttp.internal.RequestBuilder;

import okhttp3.Response;

/**
 * 请求回调基类
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */

public abstract class BaseCallback<T> {

    /**
     * 请求发起前
     *
     * 注：执行线程为请求发起线程，并不一定是UI线程
     *
     * @param requestBuilder 请求构造器
     */
    public void onBefore(RequestBuilder requestBuilder) {}

    /**
     * 上传进度回调
     *
     * @param totalSize 上传文件总大小
     * @param currentSize 当前已上传大小
     * @param progress 进度0~100
     */
    public void onUploadProgress(long totalSize, long currentSize, int progress) {}

    /**
     * 下载进度回调
     *
     * @param totalSize 下载文件总大小
     * @param currentSize 当前已下载大小
     * @param progress 进度0~100
     */
    public void onDownloadProgress(long totalSize, long currentSize, int progress) {}

    /**
     * 内容转换
     *
     * 注：异步线程
     *
     * @param response 请求返回
     * @return
     * @throws Exception
     */
    public abstract T convert(Response response) throws Exception;

    /**
     * 请求成功回调
     *
     * @param t 泛型
     */
    public abstract void onSuccess(T t);

    /**
     * 请求失败回调
     *
     * @param e 失败异常
     */
    public void onFailure(Exception e) {}

    /**
     * 请求发起后
     */
    public void onAfter() {}
}
