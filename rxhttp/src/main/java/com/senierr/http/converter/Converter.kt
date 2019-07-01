package com.senierr.http.converter

import okhttp3.Response

/**
 * 数据转换接口
 *
 * @author zhouchunjie
 * @date 2018/8/28
 */
interface Converter<T> {

    fun convertResponse(response: Response): T
}