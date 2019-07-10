package com.senierr.http.observable

import com.senierr.http.converter.Converter
import com.senierr.http.listener.OnProgressListener
import com.senierr.http.progress.Progress
import com.senierr.http.progress.ProgressBus
import com.senierr.http.progress.ProgressRequestBody
import com.senierr.http.progress.ProgressResponseBody
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

class RealObservable<T>(
        private val okHttpClient: OkHttpClient,
        private val rawRequest: Request,
        private val uploadTag: String?,
        private val downloadTag: String?,
        private val converter: Converter<T>
) : Observable<T>() {

    override fun subscribeActual(observer: Observer<in T>) {
        val disposable = CallDisposable()
        observer.onSubscribe(disposable)
        if (disposable.isDisposed) return

        var terminated = false
        try {
            val request = if (uploadTag != null) {
                // 封装请求
                wrapRequest(rawRequest, object : OnProgressListener {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        if (!disposable.isDisposed) {
                            ProgressBus.post(Progress(uploadTag, totalSize, currentSize, percent))
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
            val response = if (downloadTag != null) {
                // 封装返回
                wrapResponse(rawResponse, object : OnProgressListener {
                    override fun onProgress(totalSize: Long, currentSize: Long, percent: Int) {
                        if (!disposable.isDisposed) {
                            ProgressBus.post(Progress(downloadTag, totalSize, currentSize, percent))
                        }
                    }
                })
            } else {
                rawResponse
            }

            // 解析结果
            val t = converter.convertResponse(response)
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

    /**
     * 封装请求
     */
    private fun wrapRequest(rawRequest: Request, onUploadListener: OnProgressListener): Request {
        var requestBody = rawRequest.body()
        if (requestBody != null) {
            requestBody = ProgressRequestBody(requestBody, onUploadListener)
        }
        return rawRequest.newBuilder()
                .method(rawRequest.method(), requestBody)
                .build()
    }

    /**
     * 封装返回
     */
    private fun wrapResponse(rawResponse: Response, onDownloadListener: OnProgressListener): Response {
        var responseBody = rawResponse.body()
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