package com.senierr.http.internal;

import android.support.annotation.NonNull;

/**
 * Http请求方法构建器
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public class MethodBuilder {

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String HEAD = "HEAD";
    public static final String PATCH = "PATCH";
    public static final String OPTIONS = "OPTIONS";
    public static final String TRACE = "TRACE";

    private @NonNull String value;

    public MethodBuilder(@NonNull String value) {
        this.value = value;
    }

    public String build() {
        return value;
    }
}
