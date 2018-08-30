package com.senierr.http.internal;

import android.support.annotation.NonNull;

/**
 * Http请求方法
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public enum HttpMethod {
    GET("GET"),

    POST("POST"),

    PUT("PUT"),

    DELETE("DELETE"),

    HEAD("HEAD"),

    PATCH("PATCH"),

    OPTIONS("OPTIONS"),

    TRACE("TRACE");

    private final @NonNull String value;

    HttpMethod(@NonNull String value) {
        this.value = value;
    }

    public @NonNull String value() {
        return value;
    }

    @Override
    public @NonNull String toString() {
        return this.value;
    }

    public boolean hasBody() {
        switch (this) {
            case POST:
            case PUT:
            case PATCH:
            case DELETE:
            case OPTIONS:
                return true;
            default:
                return false;
        }
    }
}
