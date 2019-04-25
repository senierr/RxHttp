package com.senierr.http.observable;

import com.senierr.http.RxHttp;
import com.senierr.http.converter.Converter;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Request;

public final class ResultObservable<T> extends Observable<T> {

    private @NonNull RxHttp rxHttp;
    private @NonNull Request rawRequest;
    private @NonNull Converter<T> converter;

    private ResultObservable(@NonNull RxHttp rxHttp,
                             @NonNull Request request,
                             @NonNull Converter<T> converter) {
        this.rxHttp = rxHttp;
        this.rawRequest = request;
        this.converter = converter;
    }

    @NonNull
    public static <T> Observable<T> result(@NonNull RxHttp rxHttp,
                                           @NonNull Request request,
                                           @NonNull Converter<T> converter) {
        return new ResultObservable<>(rxHttp, request, converter);
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
            // 请求
            Call call = rxHttp.getOkHttpClient().newCall(rawRequest);
            disposable.call = call;
            okhttp3.Response rawResponse = call.execute();
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
