package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
import okhttp3.ResponseBody;

final class ExecuteObservable<T> extends Observable<Response<T>> {

    private @NonNull RxHttp rxHttp;
    private @NonNull HttpRequest httpRequest;
    private @NonNull Converter<T> converter;
    private @Nullable OnProgressListener onUploadListener;
    private @Nullable OnProgressListener onDownloadListener;

    ExecuteObservable(@NonNull RxHttp rxHttp,
                      @NonNull HttpRequest httpRequest,
                      @NonNull Converter<T> converter,
                      @Nullable OnProgressListener onUploadListener,
                      @Nullable OnProgressListener onDownloadListener) {
        this.rxHttp = rxHttp;
        this.httpRequest = httpRequest;
        this.converter = converter;
        this.onUploadListener = onUploadListener;
        this.onDownloadListener = onDownloadListener;
    }

    @Override
    protected void subscribeActual(final Observer<? super Response<T>> observer) {
        CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            Request request = httpRequest.generateRequest(onUploadListener, disposable);
            Call call = rxHttp.getOkHttpClient().newCall(request);
            disposable.call = call;

            okhttp3.Response rawResponse = call.execute();
            ResponseBody responseBody = rawResponse.body();
            if (responseBody != null && onDownloadListener != null) {
                rawResponse = rawResponse.newBuilder()
                        .body(new ProgressResponseBody(responseBody, onDownloadListener, disposable))
                        .build();
            }

            T t = converter.convertResponse(rawResponse);
            if (!disposable.isDisposed()) {
                observer.onNext(Response.success(call, rawResponse, t));
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
