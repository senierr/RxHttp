package com.senierr.http.observable;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;
import com.senierr.http.listener.OnProgressListener;
import com.senierr.http.model.ProgressRequestBody;
import com.senierr.http.model.ProgressResponse;
import com.senierr.http.model.ProgressResponseBody;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.SchedulerSupport;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class ProgressObservable<T> extends Observable<ProgressResponse<T>> {

    private @NonNull RxHttp rxHttp;
    private @NonNull Request rawRequest;
    private @NonNull Converter<T> converter;

    private boolean openUploadListener;
    private boolean openDownloadListener;

    private ProgressObservable(@NonNull RxHttp rxHttp,
                               @NonNull Request request,
                               boolean openUploadListener,
                               boolean openDownloadListener,
                               @NonNull Converter<T> converter) {
        this.rxHttp = rxHttp;
        this.rawRequest = request;
        this.converter = converter;
        this.openUploadListener = openUploadListener;
        this.openDownloadListener = openDownloadListener;
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @NonNull
    public static <T> Observable<ProgressResponse<T>> upload(@NonNull RxHttp rxHttp,
                                                             @NonNull Request request,
                                                             @NonNull Converter<T> converter) {
        return new ProgressObservable<>(rxHttp, request, true, false, converter);
    }

    @SchedulerSupport(SchedulerSupport.NONE)
    @NonNull
    public static <T> Observable<ProgressResponse<T>> download(@NonNull RxHttp rxHttp,
                                                               @NonNull Request request,
                                                               @NonNull Converter<T> converter) {
        return new ProgressObservable<>(rxHttp, request, false, true, converter);
    }

    @Override
    protected void subscribeActual(final Observer<? super ProgressResponse<T>> observer) {
        final CallDisposable disposable = new CallDisposable();
        observer.onSubscribe(disposable);
        if (disposable.isDisposed()) {
            return;
        }

        boolean terminated = false;
        try {
            Request request;
            if (openUploadListener) {
                // 封装请求
                request = wrapRequest(rawRequest, new OnProgressListener() {
                    @Override
                    public void onProgress(long totalSize, long currentSize, int percent) {
                        if (!disposable.isDisposed()) {
                            observer.onNext(ProgressResponse.<T>upload(totalSize, currentSize, percent));
                        }
                    }
                });
            } else {
                request = rawRequest;
            }

            // 请求
            Call call = rxHttp.getOkHttpClient().newCall(request);
            disposable.call = call;
            Response rawResponse = call.execute();
            Response response;
            if (openDownloadListener) {
                // 封装返回
                response = wrapResponse(rawResponse, new OnProgressListener() {
                    @Override
                    public void onProgress(long totalSize, long currentSize, int percent) {
                        if (!disposable.isDisposed()) {
                            observer.onNext(ProgressResponse.<T>download(totalSize, currentSize, percent));
                        }
                    }
                });
            } else {
                response = rawResponse;
            }

            // 解析结果
            T t = converter.convertResponse(response);
            if (!disposable.isDisposed()) {
                observer.onNext(ProgressResponse.result(t));
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
    private Request wrapRequest(Request rawRequest, OnProgressListener onUploadListener) {
        RequestBody requestBody = rawRequest.body();
        if (requestBody != null && onUploadListener != null) {
            requestBody = new ProgressRequestBody(requestBody, onUploadListener);
        }
        return rawRequest.newBuilder()
                .method(rawRequest.method(), requestBody)
                .build();
    }

    /**
     * 封装返回
     */
    private okhttp3.Response wrapResponse(okhttp3.Response rawResponse, OnProgressListener onDownloadListener) {
        ResponseBody responseBody = rawResponse.body();
        if (responseBody != null && onDownloadListener != null) {
            responseBody = new ProgressResponseBody(responseBody, onDownloadListener);
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
