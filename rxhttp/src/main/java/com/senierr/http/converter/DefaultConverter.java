package com.senierr.http.converter;

import android.support.annotation.NonNull;

import okhttp3.Response;

/**
 * 默认转换器
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public class DefaultConverter implements Converter<Response> {

    public @NonNull Response convertResponse(@NonNull Response response) throws Throwable {
        return response;
    }
}
