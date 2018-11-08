package com.senierr.http.internal;

import android.support.annotation.NonNull;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

final class ExecuteObservable<T> extends Observable<Object> {

    private @NonNull RxHttp rxHttp;
    private @NonNull HttpRequest httpRequest;
    private @NonNull Converter<T> converter;

    ExecuteObservable(@NonNull RxHttp rxHttp,
                      @NonNull HttpRequest httpRequest,
                      @NonNull Converter<T> converter) {
        this.rxHttp = rxHttp;
        this.httpRequest = httpRequest;
        this.converter = converter;
    }

    @Override
    protected void subscribeActual(final Observer<? super Object> observer) {
        final CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            // 创建Request
            Request.Builder requestBuilder = new Request.Builder();
            RequestBody requestBody = httpRequest.httpRequestBody.generateRequestBody();
            if (requestBody != null && httpRequest.onUploadListener != null) {
                requestBody = new ProgressRequestBody(requestBody, new OnProgressListener() {
                    @Override
                    public void onProgress(@NonNull Progress progress) {
                        if (!disposable.isDisposed()) {
                            observer.onNext(progress);
                        }
                    }
                });
            }
            requestBuilder.method(httpRequest.httpMethod.value(), requestBody);
            requestBuilder.url(httpRequest.httpUrl.generateUrl());
            requestBuilder.headers(httpRequest.httpHeaders.generateHeaders());
            Request request = requestBuilder.build();
            // 创建Call
            Call call = rxHttp.getOkHttpClient().newCall(request);
            disposable.call = call;
            // 发送请求
            okhttp3.Response rawResponse = call.execute();
            ResponseBody responseBody = rawResponse.body();
            if (responseBody != null && httpRequest.onDownloadListener != null) {
                rawResponse = rawResponse.newBuilder()
                        .body(new ProgressResponseBody(responseBody, new OnProgressListener() {
                            @Override
                            public void onProgress(@NonNull Progress progress) {
                                if (!disposable.isDisposed()) {
                                    observer.onNext(progress);
                                }
                            }
                        }))
                        .build();
            }
            // 解析请求
            T t = converter.convertResponse(rawResponse);

            if (!disposable.isDisposed()) {
                Response<T> response = new Response<>(call, rawResponse, t);
                observer.onNext(response);
            }
            if (!disposable.isDisposed()) {
                terminated = true;
                observer.onComplete();
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            if (terminated) {
                RxJavaPlugins.onError(t);
            } else if (!disposable.isDisposed()) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
            }
        }
    }

    private static final class CallDisposable implements Disposable {

        Call call;
        private volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
            if (call != null) call.cancel();
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
