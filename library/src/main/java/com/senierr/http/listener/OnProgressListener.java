package com.senierr.http.listener;

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public interface OnProgressListener {

    void onProgress(int progress, long currentSize, long totalSize);
}
