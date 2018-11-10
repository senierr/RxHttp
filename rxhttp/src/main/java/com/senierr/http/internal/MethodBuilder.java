package com.senierr.http.internal;

import android.support.annotation.NonNull;

/**
 * Http请求方法构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public class MethodBuilder {

    private @NonNull String value;

    private MethodBuilder(@NonNull String value) {
        this.value = value;
    }

    public static MethodBuilder get() {
        return new MethodBuilder("GET");
    }

    public static MethodBuilder post() {
        return new MethodBuilder("POST");
    }

    public static MethodBuilder put() {
        return new MethodBuilder("PUT");
    }

    public static MethodBuilder delete() {
        return new MethodBuilder("DELETE");
    }

    public static MethodBuilder head() {
        return new MethodBuilder("HEAD");
    }

    public static MethodBuilder patch() {
        return new MethodBuilder("PATCH");
    }

    public static MethodBuilder options() {
        return new MethodBuilder("OPTIONS");
    }

    public static MethodBuilder trace() {
        return new MethodBuilder("TRACE");
    }

    public String build() {
        return value;
    }
}
