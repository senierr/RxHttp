package com.senierr.sehttp.converter;

import okhttp3.Response;

/**
 * 转换器
 *
 * @author zhouchunjie
 * @date 2018/5/17
 */
public interface Converter<T> {

    T onConvert(Response response) throws Exception;
}
