package com.senierr.http.internal;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;

/**
 * 带进度回调的观察者
 *
 * @author zhouchunjie
 * @date 2018/11/09
 */
public interface ProgressObserver<T> extends Observer<T> {

    void onUpload(@NonNull Progress progress);

    void onDownload(@NonNull Progress progress);
}
