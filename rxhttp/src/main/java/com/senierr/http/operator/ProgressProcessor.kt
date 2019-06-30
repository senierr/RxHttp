package com.senierr.http.operator

import com.senierr.http.listener.OnProgressListener
import com.senierr.http.model.ProgressResponse
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

/**
 * 进度过滤器
 *
 * @author zhouchunjie
 * @date 2019/4/25 17:25
 */
abstract class ProgressProcessor<T> : ObservableTransformer<ProgressResponse<T>, T>, OnProgressListener {

    override fun apply(upstream: Observable<ProgressResponse<T>>): ObservableSource<T> {
        return upstream
                .filter {
                    if (it.type() == ProgressResponse.TYPE_RESULT) {
                        true
                    } else {
                        onProgress(it.totalSize(), it.currentSize(), it.percent())
                        false
                    }
                }
                .map { it.result() }
    }
}
