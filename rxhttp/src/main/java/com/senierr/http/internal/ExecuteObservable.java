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

final class ExecuteObservable<T> extends Observable<Response<T>> {

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
    protected void subscribeActual(final Observer<? super Response<T>> observer) {
        final CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            okhttp3.Response rawResponse = postRequest(observer, disposable);
            T t = convertResponse(rawResponse, observer, disposable);

            if (!disposable.isDisposed()) {
                Response<T> response = new Response<>(rawResponse, t);
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

    /**
     * 发送请求
     */
    private okhttp3.Response postRequest(final Observer observer,
                                         final CallDisposable disposable) throws IOException {
        Request rawRequest = httpRequest.generateRequest();
        String method = rawRequest.method();
        RequestBody requestBody = rawRequest.body();
        if (requestBody != null && observer instanceof ProgressObserver) {
            requestBody = new ProgressRequestBody(requestBody, new OnProgressListener() {
                @Override
                public void onProgress(@NonNull Progress progress) {
                    if (!disposable.isDisposed()) {
                        ((ProgressObserver) observer).onUpload(progress);
                    }
                }
            });
        }
        Request request = rawRequest.newBuilder()
                .method(method, requestBody)
                .build();

        Call call = rxHttp.getOkHttpClient().newCall(request);
        disposable.call = call;
        return call.execute();
    }

    /**
     * 解析结果
     */
    private T convertResponse(okhttp3.Response rawResponse,
                              final Observer observer,
                              final CallDisposable disposable) throws Throwable {
        ResponseBody responseBody = rawResponse.body();
        if (responseBody != null && observer instanceof ProgressObserver) {
            rawResponse = rawResponse.newBuilder()
                    .body(new ProgressResponseBody(responseBody, new OnProgressListener() {
                        @Override
                        public void onProgress(@NonNull Progress progress) {
                            if (!disposable.isDisposed()) {
                                ((ProgressObserver) observer).onDownload(progress);
                            }
                        }
                    }))
                    .build();
        }
        return converter.convertResponse(rawResponse);
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
