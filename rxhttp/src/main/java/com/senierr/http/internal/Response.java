package com.senierr.http.internal;

import android.support.annotation.NonNull;

import okhttp3.Headers;

/**
 * 解析结果
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public final class Response<T> {

    private @NonNull okhttp3.Response rawResponse;
    private @NonNull T body;

    public Response(@NonNull okhttp3.Response rawResponse, @NonNull T body) {
        this.rawResponse = rawResponse;
        this.body = body;
    }

    public @NonNull okhttp3.Response rawResponse() {
        return rawResponse;
    }

    public int code() {
        return rawResponse.code();
    }

    public @NonNull String message() {
        return rawResponse.message();
    }

    public @NonNull Headers headers() {
        return rawResponse.headers();
    }

    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    public @NonNull T body() {
        return body;
    }
}