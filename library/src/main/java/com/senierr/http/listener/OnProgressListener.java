package com.senierr.http.listener;

import android.support.annotation.NonNull;

import com.senierr.http.internal.Progress;

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public interface OnProgressListener {

    void onProgress(@NonNull Progress progress);
}
