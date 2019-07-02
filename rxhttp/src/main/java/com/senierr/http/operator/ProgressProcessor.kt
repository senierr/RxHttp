package com.senierr.http.operator

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
class ProgressProcessor<T>(
        private val listener: (totalSize: Long, currentSize: Long, percent: Int) -> Unit
) : ObservableTransformer<ProgressResponse<T>, T> {

    override fun apply(upstream: Observable<ProgressResponse<T>>): ObservableSource<T> {
        return upstream
                .filter {
                    if (it.type() == ProgressResponse.TYPE_RESULT) {
                        true
                    } else {
                        listener(it.totalSize(), it.currentSize(), it.percent())
                        false
                    }
                }
                .map { it.result() }
    }
}
