package com.senierr.sehttp.converter;

import okhttp3.Response;

/**
 * 数据转换接口
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
public interface Converter<T> {

    T convertResponse(Response response) throws Throwable;
}
