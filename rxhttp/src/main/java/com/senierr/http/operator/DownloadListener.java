package com.senierr.http.operator;

import com.senierr.http.model.ProgressResponse;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * @author zhouchunjie
 * @date 2019/4/25 17:25
 */
public abstract class DownloadListener<T> implements Function<ProgressResponse<T>, Observable<T>> {

    @Override
    public Observable<T> apply(ProgressResponse<T> tProgressResponse) throws Exception {
        if (tProgressResponse.type() == ProgressResponse.TYPE_DOWNLOAD) {
            onProgress(tProgressResponse.totalSize(), tProgressResponse.currentSize(), tProgressResponse.percent());
        } else if (tProgressResponse.type() == ProgressResponse.TYPE_RESULT && tProgressResponse.result() != null) {
            return Observable.just(tProgressResponse.result());
        }
        return null;
    }

    public abstract void onProgress(long totalSize, long currentSize, int percent);
}
