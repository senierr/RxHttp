package com.senierr.http.observable

import com.senierr.http.converter.Converter
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

class ResultObservable<T> private constructor(
        private val okHttpClient: OkHttpClient,
        private val rawRequest: Request,
        private val converter: Converter<T>) : Observable<T>() {

    companion object {
        fun <T> result(okHttpClient: OkHttpClient,
                       request: Request,
                       converter: Converter<T>): Observable<T> {
            return ResultObservable(okHttpClient, request, converter)
        }
    }

    override fun subscribeActual(observer: Observer<in T>) {
        val disposable = CallDisposable()
        observer.onSubscribe(disposable)
        if (disposable.isDisposed) return

        var terminated = false
        try {
            // 请求
            val call = okHttpClient.newCall(rawRequest)
            disposable.call = call
            val rawResponse = call.execute()
            // 解析结果
            val t = converter.convertResponse(rawResponse)
            if (!disposable.isDisposed) {
                observer.onNext(t)
            }
            if (!disposable.isDisposed) {
                terminated = true
                observer.onComplete()
            }
        } catch (t: Throwable) {
            Exceptions.throwIfFatal(t)
            if (terminated) {
                RxJavaPlugins.onError(t)
            } else if (!disposable.isDisposed) {
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }
            }
        }
    }

    private class CallDisposable : Disposable {
        internal var call: Call? = null
        @Volatile
        private var disposed: Boolean = false

        override fun dispose() {
            disposed = true
            call?.cancel()
        }

        override fun isDisposed(): Boolean {
            return disposed
        }
    }
}