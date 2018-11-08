package com.senierr.http.converter;

import android.support.annotation.NonNull;

import okhttp3.Response;

/**
 * 数据转换接口
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public interface Converter<T> {

    @NonNull T convertResponse(@NonNull Response response) throws Throwable;
}
