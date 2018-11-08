package com.senierr.http.internal;

import android.support.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Headers;

/**
 * 请求结果
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public final class Response<T> {

    private @NonNull Call rawCall;
    private @NonNull okhttp3.Response rawResponse;
    private @NonNull T body;

    private Response(@NonNull Call rawCall, @NonNull okhttp3.Response rawResponse, @NonNull T body) {
        this.rawCall = rawCall;
        this.rawResponse = rawResponse;
        this.body = body;
    }

    public static <T> Response<T> success(@NonNull Call rawCall, @NonNull okhttp3.Response rawResponse, T body) {
        return new Response<>(rawCall, rawResponse, body);
    }

    public @NonNull Call rawCall() {
        return rawCall;
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