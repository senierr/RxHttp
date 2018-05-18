package com.senierr.sehttp.listener;

/**
 * 下载监听
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
public interface OnDownloadListener {
    /**
     * 下载进度回调
     *
     * @param progress 进度0~100
     * @param currentSize 已下载大小
     * @param totalSize 文件总大小
     */
    void onProgress(int progress, long currentSize, long totalSize);
}
