package com.senierr.http.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;
import com.senierr.http.listener.OnProgressListener;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class ExecuteObservable<T> extends Observable<Result<T>> {

    private final RxHttp rxHttp;
    private final HttpRequest httpRequest;
    private final Converter<T> converter;
    private boolean openUploadListener;
    private boolean openDownloadListener;

    ExecuteObservable(@NonNull RxHttp rxHttp,
                      @NonNull HttpRequest httpRequest,
                      @NonNull Converter<T> converter,
                      boolean openUploadListener,
                      boolean openDownloadListener) {
        this.rxHttp = rxHttp;
        this.httpRequest = httpRequest;
        this.converter = converter;
        this.openUploadListener = openUploadListener;
        this.openDownloadListener = openDownloadListener;
    }

    @Override
    protected void subscribeActual(final Observer<? super Result<T>> observer) {
        final CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            Call call = disposable.newCall(rxHttp, httpRequest, generateUploadListener(disposable, observer));
            Response rawResponse = call.execute();
            ResponseBody responseBody = rawResponse.body();
            if (responseBody != null) {
                rawResponse = rawResponse.newBuilder()
                        .body(new ProgressResponseBody(responseBody, generateDownloadListener(disposable, observer)))
                        .build();
            }

            T t = converter.convertResponse(rawResponse);
            if (!disposable.isDisposed()) {
                observer.onNext(Result.success(rawResponse, t));
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

    private @Nullable OnProgressListener generateUploadListener(final CallDisposable disposable,
                                                                final Observer<? super Result<T>> observer) {
        if (!openUploadListener) return null;
        return new OnProgressListener() {
            @Override
            public void onProgress(@NonNull Progress progress) {
                if (!disposable.isDisposed()) {
                    observer.onNext(Result.<T>upload(progress));
                }
            }
        };
    }

    private @Nullable OnProgressListener generateDownloadListener(final CallDisposable disposable,
                                                                  final Observer<? super Result<T>> observer) {
        if (!openDownloadListener) return null;
        return new OnProgressListener() {
            @Override
            public void onProgress(@NonNull Progress progress) {
                if (!disposable.isDisposed()) {
                    observer.onNext(Result.<T>download(progress));
                }
            }
        };
    }

    private static final class CallDisposable implements Disposable {
        private Call call;
        private volatile boolean disposed;

        private Call newCall(@NonNull RxHttp rxHttp,
                                  @NonNull HttpRequest httpRequest,
                                  @Nullable OnProgressListener onProgressListener) {
            call = rxHttp.getOkHttpClient()
                    .newCall(httpRequest.generateRequest(onProgressListener));
            return call;
        }

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
