package com.senierr.http.internal;

import android.support.annotation.NonNull;

/**
 * 进度回调
 *
 * @author zhouchunjie
 * @date 2017/3/28
 */
public interface OnProgressListener {

    void onProgress(@NonNull Progress progress);
}
