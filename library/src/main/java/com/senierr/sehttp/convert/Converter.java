package com.senierr.sehttp.convert;

import okhttp3.Response;

/**
 * 内容转换器
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public interface Converter<T> {

    /**
     * 内容转换
     *
     * 注：异步线程
     *
     * @param response 请求返回
     * @return
     * @throws Exception
     */
    T convert(Response response) throws Exception;
}
