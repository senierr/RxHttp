package com.senierr.http.internal;

import android.support.annotation.NonNull;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;

import java.io.IOException;

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

final class ExecuteObservable<T> extends Observable<T> {

    private @NonNull RxHttp rxHttp;
    private @NonNull Request rawRequest;
    private @NonNull Converter<T> converter;

    ExecuteObservable(@NonNull RxHttp rxHttp,
                      @NonNull Request request,
                      @NonNull Converter<T> converter) {
        this.rxHttp = rxHttp;
        this.rawRequest = request;
        this.converter = converter;
    }

    @Override
    protected void subscribeActual(final Observer<? super T> observer) {
        final CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            // 封装请求
            Request request = wrapRequest(rawRequest, observer, disposable);
            // 请求
            Call call = rxHttp.getOkHttpClient().newCall(request);
            disposable.call = call;
            okhttp3.Response rawResponse = call.execute();
            // 封装返回
            rawResponse = wrapResponse(rawResponse, observer, disposable);
            // 解析结果
            T t = converter.convertResponse(rawResponse);
            if (!disposable.isDisposed()) {
                observer.onNext(t);
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

    /**
     * 封装请求
     */
    private Request wrapRequest(Request rawRequest, final Observer observer, final CallDisposable disposable) {
        RequestBody requestBody = rawRequest.body();
        if (requestBody != null && observer instanceof ProgressObserver) {
            requestBody = new ProgressRequestBody(requestBody, new OnProgressListener() {
                @Override
                public void onProgress(long totalSize, long currentSize, int percent) {
                    if (!disposable.isDisposed()) {
                        ((ProgressObserver) observer).onUpload(totalSize, currentSize, percent);
                    }
                }
            });
        }
        return rawRequest.newBuilder()
                .method(rawRequest.method(), requestBody)
                .build();
    }

    /**
     * 封装返回
     */
    private okhttp3.Response wrapResponse(okhttp3.Response rawResponse, final Observer observer, final CallDisposable disposable) {
        ResponseBody responseBody = rawResponse.body();
        if (responseBody != null && observer instanceof ProgressObserver) {
            responseBody = new ProgressResponseBody(responseBody, new OnProgressListener() {
                @Override
                public void onProgress(long totalSize, long currentSize, int percent) {
                    if (!disposable.isDisposed()) {
                        ((ProgressObserver) observer).onDownload(totalSize, currentSize, percent);
                    }
                }
            });
        }
        return rawResponse.newBuilder()
                .body(responseBody)
                .build();
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
