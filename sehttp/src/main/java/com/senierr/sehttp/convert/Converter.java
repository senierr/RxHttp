package com.senierr.sehttp.convert;

import okhttp3.Response;

/**
 * 转换器的作用是将Response转换成callback所需内容
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public interface Converter<T> {

    T convert(Response response) throws Exception;
}
