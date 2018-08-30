package com.senierr.http.internal;

import android.support.annotation.NonNull;

import java.util.LinkedHashMap;

import okhttp3.Headers;

/**
 * Http请求头
 *
 * @author zhouchunjie
 * @date 2018/8/29
 */
public final class HttpHeaders {

    private LinkedHashMap<String, String> httpHeaders = new LinkedHashMap<>();

    public void addHeader(@NonNull String key, @NonNull String value) {
        httpHeaders.put(key, value);
    }

    public void addHeaders(@NonNull LinkedHashMap<String, String> headers) {
        for (String key: headers.keySet()) {
            httpHeaders.put(key, headers.get(key));
        }
    }

    public @NonNull Headers generateHeaders(){
        Headers.Builder builder = new Headers.Builder();
        for (String key: httpHeaders.keySet()) {
            builder.add(key, httpHeaders.get(key));
        }
        return builder.build();
    }
}