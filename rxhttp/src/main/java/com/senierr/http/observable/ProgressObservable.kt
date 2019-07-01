package com.senierr.http.observable

import com.senierr.http.converter.Converter
import com.senierr.http.listener.OnProgressListener
import com.senierr.http.model.ProgressRequestBody
import com.senierr.http.model.ProgressResponse
import com.senierr.http.model.ProgressResponseBody
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class ProgressObservable<T> private constructor(
        private val okHttpClient: OkHttpClient,
        private val rawRequest: Request,
        private val openUploadListener: Boolean,
        private val openDownloadListener: Boolean,
        private val converter: Converter<T>
) : Observable<ProgressResponse<T>>() {

    companion object {
        fun <T> upload(okHttpClient: OkHttpClient,
                       request: Request,
                       converter: Converter<T>): Observable<ProgressResponse<T>> {
            return ProgressObservable(okHttpClient, request, true, false, converter)
        }

        fun <T> download(okHttpClient: OkHttpClient,
                         request: Request,
                         converter: Converter<T>): Observable<ProgressResponse<T>> {
            return ProgressObservable(okHttpClient, request, false, true, converter)
        }
    }

    override fun subscribeActual(observer: Observer<in ProgressResponse<T>>) {
        val disposable = CallDisposable()
        observer.onSubscribe(disposable)
        if (disposable.isDisposed) return

        var terminated = false
        try {
            val request = if (openUploadListener) {
                // 封装请求
                wrapRequest(rawRequest, object : OnProgressListener {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        if (!disposable.isDisposed) {
                            observer.onNext(ProgressResponse.upload(totalSize, currentSize, percent))
                        }
                    }
                })
            } else {
                rawRequest
            }

            // 请求
            val call = okHttpClient.newCall(request)
            disposable.call = call
            val rawResponse = call.execute()
            val response = if (openDownloadListener) {
                // 封装返回
                wrapResponse(rawResponse, object : OnProgressListener {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        if (!disposable.isDisposed) {
                            observer.onNext(ProgressResponse.download(totalSize, currentSize, percent))
                        }
                    }
                })
            } else {
                rawResponse
            }

            // 解析结果
            val t = converter.convertResponse(response)
            if (!disposable.isDisposed) {
                observer.onNext(ProgressResponse.result(t))
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

    /**
     * 封装请求
     */
    private fun wrapRequest(rawRequest: Request, onUploadListener: OnProgressListener): Request {
        var requestBody = rawRequest.body
        if (requestBody != null) {
            requestBody = ProgressRequestBody(requestBody, onUploadListener)
        }
        return rawRequest.newBuilder()
                .method(rawRequest.method, requestBody)
                .build()
    }

    /**
     * 封装返回
     */
    private fun wrapResponse(rawResponse: Response, onDownloadListener: OnProgressListener): Response {
        var responseBody = rawResponse.body
        if (responseBody != null) {
            responseBody = ProgressResponseBody(responseBody, onDownloadListener)
        }
        return rawResponse.newBuilder()
                .body(responseBody)
                .build()
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