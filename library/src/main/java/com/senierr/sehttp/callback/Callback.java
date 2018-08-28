package com.senierr.sehttp.callback;

import com.senierr.sehttp.converter.Converter;

/**
 * 请求回调基类
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public abstract class Callback<T> {

    private final Converter<T> converter;

    public Callback(Converter<T> converter) {
        this.converter = converter;
    }

    public Converter<T> converter() {
        return converter;
    }

    /**
     * 上传进度回调
     *
     * @param progress 进度0~100
     * @param currentSize 已上传大小
     * @param totalSize 文件总大小
     */
    public void onUpload(int progress, long currentSize, long totalSize) {}

    /**
     * 下载进度回调
     *
     * @param progress 进度0~100
     * @param currentSize 已下载大小
     * @param totalSize 文件总大小
     */
    public void onDownload(int progress, long currentSize, long totalSize) {}

    /**
     * 请求缓存回调
     *
     * @param t 泛型数据
     */
    public void onCacheSuccess(T t) {}

    /**
     * 请求成功回调
     *
     * @param t 泛型数据
     */
    public abstract void onSuccess(T t);

    /**
     * 请求失败回调
     *
     * @param e 失败异常
     */
    public void onFailure(Throwable e) {}
}
